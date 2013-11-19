package net.courtzabel.articles.date;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zabelc
 */
public class SimpleDateFormatFinder {

	private SimpleDateFormat sdf;
	private String regexPattern;

	public SimpleDateFormatFinder(SimpleDateFormat sdf) {
		this.sdf = sdf;
		initFormatPattern();
	}

	private void initFormatPattern() {
		DateFormatSymbols dateFormatSymbols = sdf.getDateFormatSymbols();
		
		String sdfPattern = this.sdf.toPattern();
		this.regexPattern = sdfPattern;
		
		//Replace special characters
		this.regexPattern = this.regexPattern.replaceAll("\\.", "\\\\.");
		
		//Month
		List<String> mExtr = regexExtract(sdfPattern, "(M+)");
		for (String mtch : mExtr) {
			if( mtch.length()==1 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, "1?[0-9]");
			else if (mtch.length()==2 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, "[0-1][0-9]");
			else if (mtch.length()==3)
				this.regexPattern=this.regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getShortMonths()));
			else if (mtch.length()>3)
				this.regexPattern=this.regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getMonths()) );
		}

		//Day
		List<String> dExtr = regexExtract(sdfPattern, "(d+)");
		for (String mtch : dExtr) {
			if( mtch.length()==1 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, "[1-3]?[0-9]");
			else if (mtch.length()==2 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, "[0-3][0-9]");
			else if (mtch.length()>2){
				String dRegex = "0{"+(mtch.length()-2)+"}[0-3][0-9]";
				this.regexPattern=this.regexPattern.replaceAll(mtch, dRegex);
			}
		}
		//Week Day
		List<String> wdExtr = regexExtract(sdfPattern, "(E+)");
		for (String mtch : wdExtr) {
			if( mtch.length()<=3 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getShortWeekdays()));
			else if( mtch.length()>3 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getWeekdays()));
		}
		//year
		List<String> yExtr = regexExtract(sdfPattern, "(y+)");
		for (String mtch : yExtr) {
			if (mtch.length()<=3 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, "[0-9]{2}");
			else if (mtch.length()==4 )
				this.regexPattern=this.regexPattern.replaceAll(mtch, "[0-9]{4}");
			else if (mtch.length()>4){
				String dRegex = "0{"+(mtch.length()-4)+"}[0-9]{4}";
				this.regexPattern=this.regexPattern.replaceAll(mtch, dRegex);
			}
		}	
		
		this.regexPattern = "("+this.regexPattern+")";
	}
	
	private String arrayToRegexMatch(String[] arr) {
		StringBuffer rm = new StringBuffer("(?:");//Group the values in the array, but don't capture them 
		
		for (int i = 0; i < arr.length; i++) {
			String str = arr[i];
			if(StringUtils.isNotBlank(str) ){
				if( rm.length()>3 && rm.charAt(rm.length()-1)!='|' )
					rm.append("|");
				rm.append( str );
			}
		}
		rm.append(")");
		return rm.toString();
	}

	private List<String> regexExtract(String string, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(string);
		List<String> matchedStrList = new ArrayList<String>();
		while (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				matchedStrList.add( m.group(i) );
			}
		}
		return matchedStrList;
	}

	public static class DateStringTuple{
		private String string;
		private Date date;
		public DateStringTuple(String string, Date date) {
			this.string = string;
			this.date = date;
		}
		public String getString() {
			return string;
		}
		public Date getDate() {
			return date;
		}
	}
	
	/**
	 * Extract a list of tuple of dateString and dates
	 * @param text
	 * @return
	 */
	public List<DateStringTuple> findDateTuples(String text){
		List<DateStringTuple> dateTuples = new ArrayList<DateStringTuple>();
		for (String dateStr: this.findDateStrings(text)) {
			Date date = parseDateStr(dateStr);;
			dateTuples.add( new DateStringTuple(dateStr, date) );
		}
		return dateTuples;
	}
	
	/**
	 * Extract the text for all the dates in the provided text
	 * @param text
	 * @return
	 */
	public List<String> findDateStrings(String text){
		return regexExtract(text, this.regexPattern);
	}
	
	/**
	 * Extract actual dates from the provided text
	 * @param text - String to search for Dates which match the SDF pattern
	 * @return List of Date objects.  If none or found this will be empty but not null
	 */
	public List<Date> findDates(String text){
		List<Date> dates = new ArrayList<Date>();
		
		List<String> dateStrings = findDateStrings(text);
		for (String dateStr : dateStrings) {
			dates.add( parseDateStr(dateStr) );
		}
		
		return dates;
	}

	private Date parseDateStr(String dateStr) {
		try {
			return this.sdf.parse(dateStr);
		} catch (ParseException e) {
			throw new RuntimeException("Failed to parse date string {"+dateStr+"} into date using pattern {"+this.sdf.toPattern()+"}", e);
		}
	}
}
