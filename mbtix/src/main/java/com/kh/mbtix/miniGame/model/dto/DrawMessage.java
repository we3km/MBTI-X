package com.kh.mbtix.miniGame.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class DrawMessage {
	private PathDTO path;
	private boolean isEraser;

	@Data
	public static class PathDTO {
		private String id;
		private String tool;
		private String strokeColor;
		private int strokeWidth;
		private List<Segment> segments;
	}

	@Data
	public static class Segment {
		private int x;
		private int y;
	}
}