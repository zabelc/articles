package net.courtzabel.articles.date;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

public class DateFormatExtractorSandboxTesting {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testRegex(){
		String string = "January 12, 2013";
		String pattern = "((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec) [1-3]?[0-9], [0-9]{4})";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(string);
		List<String> matchedStrList = new ArrayList<String>();
		while (m.find()) {
			for (int i = 1; i <= m.groupCount(); i++) {
				matchedStrList.add( m.group(i) );
			}
		}
		System.out.println(matchedStrList);
	}
	
	@Test
	public void testOut(){
		SimpleDateFormat sdf= new SimpleDateFormat("EEE");
		System.out.println( sdf.format(new Date()) );
	}
	
	@Test
	public void test() {
		String sdfPattern = "MMM d, yyyy";
		String regexPattern = sdfPattern;
		SimpleDateFormat sdf = new SimpleDateFormat(sdfPattern);
		DateFormatSymbols dateFormatSymbols = sdf.getDateFormatSymbols();
		
		//Month
		List<String> mExtr = regexExtract(sdfPattern, "(M+)");
		for (String mtch : mExtr) {
			if( mtch.length()==1 )
				regexPattern=regexPattern.replaceAll(mtch, "1?[0-9]");
			else if (mtch.length()==2 )
				regexPattern=regexPattern.replaceAll(mtch, "[0-1][0-9]");
			else if (mtch.length()==3)
				regexPattern=regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getShortMonths()));
			else if (mtch.length()>3)
				regexPattern=regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getMonths()) );
		}

		//Day
		List<String> dExtr = regexExtract(sdfPattern, "(d+)");
		for (String mtch : dExtr) {
			if( mtch.length()==1 )
				regexPattern=regexPattern.replaceAll(mtch, "[1-3]?[0-9]");
			else if (mtch.length()==2 )
				regexPattern=regexPattern.replaceAll(mtch, "[0-3][0-9]");
			else if (mtch.length()>2){
				String dRegex = "0{"+(mtch.length()-2)+"}[0-3][0-9]";
				regexPattern=regexPattern.replaceAll(mtch, dRegex);
			}
		}
		//Week Day
		List<String> wdExtr = regexExtract(sdfPattern, "(E+)");
		for (String mtch : wdExtr) {
			if( mtch.length()<=3 )
				regexPattern=regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getShortWeekdays()));
			else if( mtch.length()>3 )
				regexPattern=regexPattern.replaceAll(mtch, arrayToRegexMatch(dateFormatSymbols.getWeekdays()));
		}
		//year
		List<String> yExtr = regexExtract(sdfPattern, "(y+)");
		for (String mtch : yExtr) {
			if (mtch.length()<=3 )
				regexPattern=regexPattern.replaceAll(mtch, "[0-9]{2}");
			else if (mtch.length()==4 )
				regexPattern=regexPattern.replaceAll(mtch, "[0-9]{4}");
			else if (mtch.length()>4){
				String dRegex = "0{"+(mtch.length()-4)+"}[0-9]{4}";
				regexPattern=regexPattern.replaceAll(mtch, dRegex);
			}
		}
		System.out.println(regexPattern);
	}

	private String arrayToRegexMatch(String[] arr) {
		StringBuffer rm = new StringBuffer("(?:");
		
		for (int i = 0; i < arr.length; i++) {
			String str = arr[i];
			if(StringUtils.isNotBlank(str) ){
				if( rm.length()>2 && rm.charAt(rm.length()-1)!='|' )
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
	@Test
	public void testRegexExtract(){
		String string = "January  f";//"January 12, 2013";
		String pattern = "((?:January|February) )";//"((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)? [1-3]?[0-9], [0-9]{4})";
		List<String> extract = regexExtract(string, pattern);
		System.out.println(extract);
	}
}
