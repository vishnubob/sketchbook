Grid grid = new Grid();

public class Bob
{
    final private int UP = 1;
    final private int DOWN = 2;
    final private int LEFT = 3;
    final private int RIGHT = 4;
    final private int DIRS[]  = {UP, DOWN, LEFT, RIGHT};

    private int _x;
    private int _y;
    private Grid _grid;
    private int rgb[];

    public void init(Grid grid, int x, int y)
    {
        _grid = grid;
        _x = x;
        _y = y;
        rgb = new int[3];
        for(int idx = 0; idx < 3; ++idx)
        {
            rgb[idx] = (int)random(255);
        }
    }

    public boolean step()
    {
        // make a copy of the array
        int dirs[] = shuffle(DIRS);
        boolean alive = false;
        for(int idx = 0; idx < dirs.length; idx++)
        {
            int new_x = _x;
            int new_y = _y;
            switch(dirs[idx])
            {
                case UP:
                    new_x--;
                    break;
                case DOWN:
                    new_x++;
                    break;
                case LEFT:
                    new_y--;
                    break;
                case RIGHT:
                    new_y++;
                    break;
            }
            fill(rgb[0], rgb[1], rgb[2]);
            if (_grid.move(_x, _y, new_x, new_y))
            {
                alive = true;
                _x = new_x;
                _y = new_y;
                break;
            }
        }
        return alive;
    }

}

public class Grid
{
    private int _interval;
    private int _nodes;
    private int _grid[][];

    public void init(int interval)
    {
        _interval = interval;
        _nodes = (width - _interval / 2) / (2 * _interval) + 1;
        _grid = new int[_nodes][_nodes];
        print("Nodes: ");
        println(_nodes);
        draw_grid();
    }

    public boolean move(int cur_x, int cur_y, int next_x, int next_y)
    {
        _grid[cur_x][cur_y] = 1;
        if (next_x < 0 || next_x >= _nodes || next_y < 0 || next_y >= _nodes) 
        {
            return false;
        }
        if (_grid[next_x][next_y] == 1)
        {
            return false;
        }
        print("move: ");
        print(cur_x);
        print(" ");
        print(cur_y);
        print(" ");
        print(next_x);
        print(" ");
        print(next_y);
        println("");

        _grid[next_x][next_y] = 1;
        int offset = (_interval / 2);

        int delta_x = next_x - cur_x;
        int delta_y = next_y - cur_y;

        int x = 2 * cur_x * _interval + offset + (delta_x * _interval);
        int y = 2 * cur_y * _interval + offset + (delta_y * _interval);
        int width = (abs(delta_x) * 2 + 1) * _interval;
        int height = (abs(delta_y) * 2 + 1) * _interval;
        rect(x, y, width, height);
        return true;
    }

    public void draw_grid()
    {
        rectMode(CENTER);

        for(int y = (_interval / 2); y <= height; y += (2 * _interval))
        {
            for(int x = (_interval / 2); x <= width; x += (2 * _interval))
            {
                rect(x, y, _interval, _interval);
            }
        }
    }
}

Bob bobs[];
int[] grid_points;
int grid_point_idx = 0;
int node_cnt;

void setup() 
{
    //noCursor();
    noStroke();
    fill(100, 100, 100);
    int interval = 1;
    node_cnt = 200;
    int wh = interval * (2 * node_cnt - 1);
    size(wh, wh);
    grid.init(interval);
    frameRate(30);
    int bobcnt = 20;
    grid_points = new int[node_cnt * node_cnt];
    for(int x = 0; x < grid_points.length; ++x)
    {
        grid_points[x] = x;
    }
    grid_points = shuffle(grid_points);
    
    bobs = new Bob[bobcnt];
    for(int idx = 0; idx < bobs.length; ++idx)
    {
        grid_point_idx++;
        int px = grid_points[grid_point_idx];
        int x = px / node_cnt;
        int y = px % node_cnt;
        bobs[idx] = new Bob();
        bobs[idx].init(grid, x, y);
    }
}

boolean key_pressed = false;
void keyPressed()
{
    key_pressed = true;
}

void draw() 
{
    /*
    while(key_pressed == false)
        return;
    key_pressed = false;
    */
    for(int idx = 0; idx < bobs.length; ++idx)
    {
        boolean alive = bobs[idx].step();
        if (!alive)
        {
            if (grid_point_idx < grid_points.length)
            {
                grid_point_idx++;
                int px = grid_points[grid_point_idx];
                int x = px / node_cnt;
                int y = px % node_cnt;
                bobs[idx].init(grid, x, y);
            }
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

int[] shuffle(int deck[])
{
    int elems = deck.length;
    int[] deckcopy = new int[elems];
    int[] newdeck = new int[elems];
    arrayCopy(deck, deckcopy);

    for(int x = 0; x < elems; x++)
    {
        int idx = (int)random(0, elems);
        newdeck[x] = deckcopy[idx];
        for( ; idx < (elems - 1); idx++)
        {
            deckcopy[idx] = deckcopy[idx + 1];
        }
    }
    return newdeck;
}

