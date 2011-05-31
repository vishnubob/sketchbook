import sprites.*;

final int EYE_COUNT = 6;

PImage[]    images;
String[]    files;
int         prev_img;
int         next_img;
int         timer;
ImageFader  imf = new ImageFader();

// sprites
Sprite[] eyes = new Sprite[EYE_COUNT];


void setup() 
{
    size(700, 700);
    files = new String[EYE_COUNT];
    images = new PImage[EYE_COUNT];

    for(int idx = 0; idx < EYE_COUNT; ++idx)
    {
        String fn = "eye" + (idx + 1) + ".jpg";
        eyes[idx] = Sprite(this, fn, 0);
    }

    randomSeed(0);
    prev_img = 0;
    timer = 0;
    _state = 0;
}

public class ImageFader
{
    private PImage _img1;
    private PImage _img2;
    private PImage _canvas;
    private int _length;
    private int _draw_idx;
    private double _step;

    public void load(PImage img1, PImage img2, int length)
    {
        _img1 = img1;
        _img2 = img2;
        _length = length;
        _step = 1.0 / _length;
        _draw_idx = 0;
        assert (_img1.width == _img2.width);
        assert (_img1.height == _img2.height);
        try
        {
            _canvas = (PImage)_img1.clone();
        } catch (java.lang.CloneNotSupportedException ex)
        {
            return;
        }
        //_canvas.resize(_img1.width, _img1.height);
        //_img1.copy(_canvas, 0, 0, _img1.width, _img1.height, 0, 0, _img1.width, _img1.height);
    }

    public boolean is_drawing()
    {
        return (_draw_idx < _length);
    }

    public PImage draw()
    {
        int pixel_cnt = _img1.width * _img1.height;
        _canvas.loadPixels();
        for(int px = 0; px < pixel_cnt; ++px)
        {
            int rgb1[] = unpack_rgb(_img1.pixels[px]);
            int rgb2[] = unpack_rgb(_img2.pixels[px]);
            scale_rgb_array(rgb1, 1.0 - (_step * _draw_idx));
            scale_rgb_array(rgb2, (_step * _draw_idx));
            for(int idx = 0; idx < 3; ++idx)
            {
                rgb1[idx] = min(rgb1[idx] + rgb2[idx], 0xFF);
            }
            _canvas.pixels[px] = pack_rgb(rgb1);
        }
        _canvas.updatePixels();
        _draw_idx += 1;
        return _canvas;
    }
}

int _state;

void draw() 
{
    if(_state == 0)
    {
        prev_img = next_img;
        /*
        while(prev_img == next_img)
        {
            next_img = (int)random(3);
        }
        */
        next_img += 1;
        next_img %= 3;
        text("P " + prev_img + " N " + next_img, 20, 20);
        imf.load(images[prev_img], images[next_img], 60 * 10);
        _state = 1;
        //background(0);
        //image(next_img, 0, 0, width, height);
    } else
    {
        if(imf.is_drawing())
        {
            //background(0);
            image(imf.draw(), 0, 0, width, height);
            text("P " + prev_img + " N " + next_img, 20, 20);
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

