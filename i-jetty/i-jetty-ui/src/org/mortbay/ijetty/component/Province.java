package org.mortbay.ijetty.component;

import java.util.List;

public class Province {
	public static class City {
		private String name;
		private long id;

		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return "City [name=" + name + ", id=" + id + "]\n";
		}

	}

	private String proName;
	private List<City> citys;
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProName() {
		return proName;
	}

	public void setProName(String proName) {
		this.proName = proName;
	}

	public List<City> getCitys() {
		return citys;
	}

	public void setCitys(List<City> citys) {
		this.citys = citys;
	}

	@Override
	public String toString() {
		return "Province [proName=" + proName + ", citys=" + citys + ", id="
				+ id + "]";
	}

}
