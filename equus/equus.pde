import fullscreen.*; 
import processing.video.*;

final int EYE_COUNT = 6;

int         prev_img;
int         next_img;
int         timer;
int         _state;
boolean     AllStop = true;
boolean     DrawField = false;
boolean     DrawTV = false;
float       FadeLevel;
float       DrawEyes;
PImage[]    images = new PImage[EYE_COUNT];
PImage      field_image; 
EquusImage  ek = new EquusImage();
FullScreen  fs; 
Movie       tv_movie;

public class EquusImage
{
    private PImage _image;
    private PImage _canvas;
    private int _length;
    private int _pixel_cnt;
    private int _frame_number;
    private int _one_third;
    private int _state;
    private int _fade_cnt;
    private int _visible_timeout;
    private double _fade_step;
    private boolean _alive;
    /* scale */
    private float _scale;
    private float _orig_scale;
    private float _final_scale;
    private float _step_scale;
    /* movement */
    int _x_offset;
    int _y_offset;
    double _x_vector;
    double _y_vector;
    /* rotation */
    float _step_rotate = 0;
    float _rotate = 0;

    public void init(PImage img, int length, float speed)
    {
        _image = img;
        _visible_timeout = 2;
        _pixel_cnt = _image.width * _image.height;
        _length = length;
        _one_third = length / 3;
        _fade_step = 1.0 / _one_third;
        _frame_number = 0;
        _alive = true;
        _state = 0;
        _fade_cnt = 0;
        _canvas = new PImage(_image.width, _image.height);
        _orig_scale = random(1, 11) / 10.0;
        _final_scale = random(1, 21) / 10.0;
        _step_scale = (_final_scale - _orig_scale) / (float)_length;
        //_x_offset = random((_image.width / -4), width + (_image.width / 4));
        //_y_offset = random((_image.height / -4), height + (_image.height / 4));
        int _width_scale = (int)((float)_image.width * _orig_scale / 2.0);
        int _height_scale = (int)((float)_image.height * _orig_scale / 2.0);
        _x_offset = (width / 2) - _width_scale + (int)random(-10, 10);
        _y_offset = (height / 2) - _height_scale + (int)random(-10, 10);
        /* X vector */
        if ((_x_offset + _width_scale) < (width / 2.0))
        {
            _x_vector = random(1, 30) / 20.0;
        } else
        {
            _x_vector = random(1, 30) / -20.0;
        }
        /* Y vector */
        if ((_y_offset + _height_scale) <= (height / 2.0))
        {
            _y_vector = random(1, 30) / 20.0;
        } else
        {
            _y_vector = random(1, 30) / -20.0;
        }
        /* speed */
        _x_vector *= speed;
        _y_vector *= speed;
        /*
        print("Offset: ");
        print(_x_offset);
        print(" ");
        println(_y_offset);
        print("Speed: ");
        print(_x_vector);
        print(" ");
        println(_y_vector);
        */
        /* rotate */
        _step_rotate = ((2 * PI) / random(2000, 4000)) * random(-1, 2);
        _step_rotate *= speed;
        _rotate = 0;
    }

    public boolean is_alive()
    {
        if (_state > 0 && _visible_timeout < 0)
        {
            return false;
        }
        return _alive;
    }

    public boolean is_visible()
    {
        // XXX: this is stupid
        loadPixels();
        for(int i = 0; i < (width * height); ++i)
        {
            int pixel = pixels[i];
            if (red(pixel) != 0.0 || green(pixel) != 0.0 || blue(pixel) != 0.0)
            { 
                return true; 
            }
        }
        return false;
    }

    public void draw()
    {
        if (_state == 0)
        {
            fade_in();
        } else
        if (_state == 1)
        {
        } else
        if (_state == 2)
        {
            fade_out();
        } else
        {
            _alive = false;
        }
        _frame_number += 1;
        if (_frame_number > (_one_third * (_state + 1)))
        {
            _state += 1;
            _fade_cnt = 0;
        }
        pushMatrix();
        _scale = _step_scale * _frame_number + _orig_scale;
        _x_offset += _x_vector;
        _y_offset += _y_vector;
        scale(_scale);
        _rotate += _step_rotate;
        rotate(_rotate);
        image(_canvas, (int)_x_offset, (int)_y_offset, _canvas.width, _canvas.height);
        popMatrix();
        if(!is_visible())
        {
            _visible_timeout -= 1;
        }
    }

    private void fade_in()
    {
        _canvas.loadPixels();
        for(int px = 0; px < _pixel_cnt; ++px)
        {
            int rgb[] = unpack_rgb(_image.pixels[px]);
            scale_rgb_array(rgb, _fade_step * _frame_number);
            _canvas.pixels[px] = pack_rgb(rgb);
        }
        _canvas.updatePixels();
        _fade_cnt += 1;
    }

    private void fade_out()
    {
        _canvas.loadPixels();
        for(int px = 0; px < _pixel_cnt; ++px)
        {
            int rgb[] = unpack_rgb(_image.pixels[px]);
            scale_rgb_array(rgb, 1 - _fade_step * _fade_cnt);
            _canvas.pixels[px] = pack_rgb(rgb);
        }
        _canvas.updatePixels();
        _fade_cnt += 1;
    }
}

void setup() 
{
    noCursor();
    size(1280, 720);
    frameRate(30);
    fs = new FullScreen(this);
    fs.enter();

    for(int idx = 0; idx < EYE_COUNT; ++idx)
    {
        String fn = "horseeyes" + (idx + 1) + ".png";
        images[idx] = loadImage(fn);
    }
    field_image = loadImage("pond2.jpg");
    tv_movie = new Movie(this, "gijoe2.m4v");
    tv_movie.stop();

    prev_img = 0;
    timer = 0;
    _state = 0;
}

void movieEvent(Movie m) 
{
    m.read();
}

void keyPressed()
{
    background(0);
    if (key == ' ')
    {
        AllStop = true;
        FadeLevel = 1;
        DrawEyes = 0.0;
        DrawField = false;
        DrawTV = false;
        tv_movie.stop();
    } else
    if (key == 'f')
    {
        AllStop = false;
        DrawField = true;
        DrawEyes = 0.0;
        DrawTV = false;
        tv_movie.stop();
    } else
    if (key == 't')
    {
        AllStop = false;
        DrawTV = true;
        DrawEyes = 0.0;
        DrawField = false;
        tv_movie.read();
        tv_movie.play();
        tv_movie.loop();
    } else
    if (key == '1')
    {
        AllStop = false;
        _state = 0;
        DrawEyes = 1.0;
        tv_movie.stop();
        DrawField = false;
        DrawTV = false;
    } else
    if (key == '2')
    {
        AllStop = false;
        DrawEyes = 5.0;
        _state = 0;
        tv_movie.stop();
        DrawField = false;
        DrawTV = false;
    } else
    if (key == '3')
    {
        AllStop = false;
        DrawEyes = 15.0;
        _state = 0;
        tv_movie.stop();
        DrawField = false;
        DrawTV = false;
    }
}

void draw() 
{
    if(AllStop)
    {
        /*
        if (FadeLevel >= 0)
        {
            FadeLevel -= .1;
            fade_out_screen(FadeLevel);
            return;
        } else
        {
            background(0);
            return;
        }
        */
        background(0);
        return;
    }
    
    if(DrawField)
    {
        image(field_image, 0, 0, field_image.width, field_image.height);
    } else
    if(DrawTV)
    {
        //image(tv_movie, -200, height - 30);
        pushMatrix();
        translate(0, height - 50);
        scale(.15);
        image(tv_movie, 0, 0);
        popMatrix();
    } else
    if(DrawEyes > 0)
    {
        if(_state == 0)
        {
            prev_img = next_img;
            while(prev_img == next_img)
            {
                next_img = (int)random(EYE_COUNT);
            }
            int frames = (int)random(200, 500);
            frames /= DrawEyes;
            ek.init(images[next_img], frames, DrawEyes);
            _state = 1;
            background(0);
        } else
        {
            if(ek.is_alive())
            {
                background(0);
                ek.draw();
            } else
            {
                _state = 0;
            }
        }
    }
}

void fade_out_screen(float value)
{
    loadPixels();
    for(int px = 0; px < (width * height); ++px)
    {
        int rgb[] = unpack_rgb(pixels[px]);
        scale_rgb_array(rgb, value);
        pixels[px] = pack_rgb(rgb);
    }
    updatePixels();
}


int[] unpack_rgb(int color_value)
{
    int rgb[] = new int[3];
    rgb[0] = (color_value >> 16) & 0xFF;
    rgb[1] = (color_value >> 8) & 0xFF;
    rgb[2] = color_value & 0xFF;
    return rgb;
}

int pack_rgb(int[] rgb)
{
    return (min(rgb[0], 0xFF) << 16) | (min(rgb[1], 0xFF) << 8) | min(rgb[2], 0xFF);
}

int scale_rgb(int rgb, double scale)
{
    int rgb_values[] = unpack_rgb(rgb);
    scale_rgb_array(rgb_values, scale);
    return pack_rgb(rgb_values);
}

void scale_rgb_array(int rgb[], double scale)
{
    for (int idx = 0; idx < 3; ++idx)
    {
        rgb[idx] *= scale;
    }
}
