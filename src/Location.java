import java.util.Objects;

public class Location
{
    private int sheet;
    private int row;
    private int col;

    /** Sets x, y, z to the recieved values */
    public Location(int col, int row, int sheet)
    {
        this.col   = col;
        this.row   = row;
        this.sheet = sheet;
    }

    public int getCol()
    {   return col;   }

    public int getRow()
    {   return row;   }

    public int getSheet()
    {   return sheet;   }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location l = (Location) o;
        return col == l.col && row == l.row && sheet == l.sheet;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row, sheet);
    }

    public String toString()
    {   return "(col=" + col + ",row=" + row + ",sheet=" + sheet + ")";   }
}