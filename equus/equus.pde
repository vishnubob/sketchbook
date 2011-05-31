import fullscreen.*; 

final int EYE_COUNT = 6;

int         prev_img;
int         next_img;
int         timer;
int         _state;
PImage[]    images = new PImage[EYE_COUNT];
EquusImage  ek = new EquusImage();
FullScreen  fs; 

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
    double _x_offset;
    double _y_offset;
    double _x_vector;
    double _y_vector;
    /* rotation */
    float _step_rotate = 0;
    float _rotate = 0;

    public void init(PImage img, int length)
    {
        _image = img;
        _visible_timeout = 5;
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
        _x_offset = random((_image.width / -2), width + (_image.width / 2));
        _y_offset = random((_image.height / -2), height + (_image.height / 2));
        /* X vector */
        if (_x_offset <= _image.width)
        {
            _x_vector = random(0, 11) / 20.0;
        } else
        if (_x_offset >= (width - _image.width))
        {
            _x_vector = random(0, 11) / -20.0;
        } else
        {
            _x_vector = random(-10, 11) / 20.0;
        }
        /* Y vector */
        if (_y_offset <= _image.height)
        {
            _y_vector = random(0, 11) / 20.0;
        } else
        if (_y_offset >= (height - _image.height))
        {
            _y_vector = random(0, 11) / -20.0;
        } else
        {
            _y_vector = random(-10, 11) / 20.0;
        }
        /* rotate */
        _step_rotate = ((2 * PI) / random(1000, 2000)) * random(-1, 2);
        _rotate = 0;
    }

    public boolean is_alive()
    {
        return _alive && (_visible_timeout > 0);
    }

    public boolean is_visible()
    {
        // XXX: this is stupid
        loadPixels();
        for(int i = 0; i < (width * height); ++i)
        {
            if (pixels[i] != 0) { return true; }
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
    size(640, 480);
    frameRate(30);
    fs = new FullScreen(this);
    //fs.enter();

    for(int idx = 0; idx < EYE_COUNT; ++idx)
    {
        String fn = "eye" + (idx + 1) + ".jpg";
        images[idx] = loadImage(fn);
    }

    prev_img = 0;
    timer = 0;
    _state = 0;
}

void draw() 
{
    if(_state == 0)
    {
        prev_img = next_img;
        while(prev_img == next_img)
        {
            next_img = (int)random(EYE_COUNT);
        }
        ek.init(images[next_img], (int)random(100, 500));
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
