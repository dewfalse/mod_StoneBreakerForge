package mod_StoneBreaker;

public class Position {
	public int x;
	public int y;
	public int z;

	public Position(long l, long m, long n) {
		this.x = (int) l;
		this.y = (int) m;
		this.z = (int) n;
	}

	public Position(Position pos) {
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
	}

	public void set(int x2, int y2, int z2) {
		this.x = x2;
		this.y = y2;
		this.z = z2;
	}

	public void set(Position pos) {
		this.x = pos.x;
		this.y = pos.y;
		this.z = pos.z;
	}

	public Position add(int x2, int y2, int z2) {
		this.x += x2;
		this.y += y2;
		this.z += z2;
		return this;
	}

	public Position add(Position pos) {
		this.x += pos.x;
		this.y += pos.y;
		this.z += pos.z;
		return this;
	}

    public String toString()
    {
        return (new StringBuilder()).append("(").append(x).append(", ").append(y).append(", ").append(z).append(")").toString();
    }

    @Override
    public int hashCode() {
    	return 13 * 13 * x + 13 * y + z;
    }

    @Override
    public boolean equals(Object obj) {
    	if(obj == null) return false;
    	if(obj instanceof Position) {
    		Position pos = (Position)obj;
    		if(x == pos.x && y == pos.y && z == pos.z) {
    			return true;
    		}
    	}
    	return false;
    }
}
