package edu.wwu.cs397.graphdroid_ngon2_furerm.MathGraphe;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Pair;

/**
 * This class presents the cache of each function
 * as it is bascily a list of <double, double>
 * @author ngon2
 *
 */
public class FuncValueCache extends HashMap<Double, Double>{
	
	public int screenWidth = 0; // tested value, but the screen should not change
	public double step;
	
	public FuncValueCache(int screenWidth, double step)
	{
		super();
		this.screenWidth = screenWidth;
		this.step = step;
	}
	
	/**
	 * test if the domain is already in in the cached. 
	 * 
	 * @param domain
	 * @return zero List if the domain is complete in the cached and can just query
	 * otherwise return an array of values that are not in the cached.
	 */
	public ArrayList<Double> getNotCoverage(Pair<Double, Double> domain)
	{
		ArrayList<Double> result = new ArrayList<Double>();
		double step = Math.abs(domain.first - domain.second) / ((Integer)screenWidth).doubleValue();
		for (double i = domain.first; i < domain.second; i += step)
		{
			if (!containsKey(i) || get(i) == null)
				result.add(i);
		}		
		return result;
	}
	
	public ArrayList<Double> getCoverage(Pair<Double, Double> domain)
	{
		ArrayList<Double> result = new ArrayList<Double>();
		
		for (double i = domain.first; i < domain.second; i += step)
		{
			if (containsKey(i) || get(i) == null)
				result.add(i);
		}		
		return result;
		
	}
}