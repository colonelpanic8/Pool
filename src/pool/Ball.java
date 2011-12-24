package pool;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Ball{
	double xv, yv;
	double xp, yp;
	double xf, yf;
	double xcue, ycue;
	int size;
	Color color;
	boolean sel;
	public Ball(Color col, int x, int y, int a, int b, int s){
		xp = x;
		yp = y;
		color = col;
		xv = a;
		yv = b;
		size = s;
	}
	public int getcx(){
		return (int)xp + size/2;
	}
	public int getcy(){
		return (int)yp + size/2;
	}
}