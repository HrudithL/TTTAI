public class Location
{
    int sheet;
    int row;
    int col;

    /** Sets x, y, z to the recieved values */
    public Location(int col,int row,int sheet)
    {
        this.col    = col;
        this.row    = row;
        this.sheet     = sheet;
    }

    public int getCol()
    {   return col;   }


    public int getRow()
    {   return row;   }


    public int getSheet()
    {   return sheet;   }


    public String toString()
    {   return "(x=" + col+",y="+row+ ",z="+sheet+ ")";   }
}