
package net.courtzabel.articles.date-extraction;

import static org.junit.Assert.*;

import gov.sec.www.migrate.extract.fields.SimpleDateFormatFinder.DateStringTuple;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StringUtils;

public class SimpleDateFormatFinderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test_yyyy_MM_dd() {
		runTest("yyyy MM dd");
	}
	@Test
	public void test_yyyy_sl_MM_sl_dd() {
		runTest("yyyy/MM/dd");
	}
	@Test
	public void test_yyyy_M_d() {
		runTest("yyyy M d");
	}
	@Test
	public void test_yyyy_sl_M_sl_d() {
		runTest("yyyy/M/d");
	}

	@Test
	public void test_MMM_d_yyyy() {
		runTest("MMM d yyyy");
	}
	@Test
	public void test_MMM_dot_d_c_yyyy() {
		runTest("MMM. d, yyyy");
	}
	@Test
	public void test_MMMM_d_c_yyyy() {
		runTest("MMMM d, yyyy");
	}

	@Test
	public void test_d_MMMM_yyyy() {
		runTest("d MMMM yyyy");
	}

	private void runTest(String format){
		
		runTest(format, "DATE");
		runTest(format, "This is the DATE with text after it");
		runTest(format, "This is the DATE with text after it and repeated DATE");
		runTest(format, "<p>This is the DATE with markup</p>");
		runTest(format, "<p>This is the </br> \n DATE with markup</p>");
	}
	
	private void runTest(String format, String data){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		
		Date testDate = new Date();
		String testDateStr = sdf.format(testDate);
		
		String testData = data.replaceAll("DATE", testDateStr);
		
		SimpleDateFormatFinder sdfFinder = new SimpleDateFormatFinder(sdf);
		
		List<Date> dates = sdfFinder.findDates(testData);
		
		assertNotNull(dates);
		assertEquals( StringUtils.countOccurrencesOf(data,"DATE"), dates.size());
		for (Date date : dates) {
			compareDates(testDate, date);
		}
	}

	@SuppressWarnings("deprecation")
	private void compareDates(Date testDate, Date date) {
		assertEquals("Years not the same",testDate.getYear(), date.getYear());
		assertEquals("Days not the same",testDate.getDay(), date.getDay());
		assertEquals("Years not the same",testDate.getYear(), date.getYear());
	}
	
	@Test
	public void testNominalFailure_FullMonthWithDotShortPattern(){
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("MMM. d, yyyy");
		SimpleDateFormatFinder sdfF1 = new SimpleDateFormatFinder(sdf1);
		
		String dateStr = "January 27, 2012";
		List<DateStringTuple> dateTuples = sdfF1.findDateTuples(dateStr);
		assertTrue( dateTuples.isEmpty() );
	}
	@Test
	public void testNominalFailure_FullMonthWithShortPattern(){
		
		SimpleDateFormat sdf1 = new SimpleDateFormat("MMM d, yyyy");
		SimpleDateFormatFinder sdfF1 = new SimpleDateFormatFinder(sdf1);
		
		String dateStr = "January 27, 2012";
		List<DateStringTuple> dateTuples = sdfF1.findDateTuples(dateStr);
		assertTrue( dateTuples.isEmpty() );
	}
	
	@Test
	public void testMultipleDatesInString(){
		//Create a SimpleDateFormat and pass that to the new SimpleDateFormatFinder 
		SimpleDateFormat sdf = new SimpleDateFormat("d MMMM yyyy");
		SimpleDateFormatFinder sdfFinder = new SimpleDateFormatFinder(sdf);

		//Use the SimpleDateFormatFinder to find dates in a string.
		List<Date> dates = sdfFinder.findDates("The Grand Teton was first summited on 11 August 1898 via the Owen-Spalding Route"
							+"and on 15 July 1931 via the Exum Route.");

		assertEquals(2, dates.size());
		assertEquals("11 August 1898", sdf.format(dates.get(0)) );
		assertEquals("15 July 1931", sdf.format(dates.get(1)) );
	}
}
