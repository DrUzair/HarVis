package yt.har;

import javax.swing.JTextArea;

public class UoD_Param{	/**
	 * @param uodOutputTextArea
	 * @param wordCountLimit
	 * @param coWordCountLimit
	 * @param coOccurCountLimit
	 * @param srcTable
	 * @param destTable
	 * @param uodKeywordFilter
	 */
	public UoD_Param(JTextArea uodOutputTextArea, int wordCountLimit,
			int coWordCountLimit, int coOccurCountLimit, String srcTable,
			String destTable, String uodKeywordFilter) {
		this.uodOutputTextArea = uodOutputTextArea;
		this.wordCountLimit = wordCountLimit;
		this.coWordCountLimit = coWordCountLimit;
		this.coOccurCountLimit = coOccurCountLimit;
		this.srcTable = srcTable;
		this.destTable = destTable;
		this.setUodKeywordFilter(uodKeywordFilter);
	}
	/**
	 * @param wordCountLimit
	 * @param coWordCountLimit
	 * @param coOccurCountLimit
	 * @param srcTable
	 * @param destTable
	 */
	public UoD_Param(int wordCountLimit, int coWordCountLimit,
			int coOccurCountLimit, String srcTable, String destTable) {
		this.wordCountLimit = wordCountLimit;
		this.coWordCountLimit = coWordCountLimit;
		this.coOccurCountLimit = coOccurCountLimit;
		this.srcTable = srcTable;
		this.destTable = destTable;
	}
	JTextArea uodOutputTextArea;
	private int wordCountLimit;
	private int coWordCountLimit;
	private int coOccurCountLimit;
	private String srcTable;
	private String destTable;
	private String uodKeywordFilter = null;
	public int getWordCountLimit() {
		return wordCountLimit;
	}
	public void setWordCountLimit(int wordCountLimit) {
		this.wordCountLimit = wordCountLimit;
	}
	public int getCoWordCountLimit() {
		return coWordCountLimit;
	}
	public void setCoWordCountLimit(int coWordCountLimit) {
		this.coWordCountLimit = coWordCountLimit;
	}
	public int getCoOccurCountLimit() {
		return coOccurCountLimit;
	}
	public void setCoOccurCountLimit(int coOccurCountLimit) {
		this.coOccurCountLimit = coOccurCountLimit;
	}
	public String getSrcTable() {
		return srcTable;
	}
	public void setSrcTable(String srcTable) {
		this.srcTable = srcTable;
	}
	public String getDestTable() {
		return destTable;
	}
	public void setDestTable(String destTable) {
		this.destTable = destTable;
	}
	/**
	 * @return the uodKeywordFilter
	 */
	public String getUodKeywordFilter() {
		return uodKeywordFilter;
	}
	/**
	 * @param uodKeywordFilter the uodKeywordFilter to set
	 */
	public void setUodKeywordFilter(String uodKeywordFilter) {
		this.uodKeywordFilter = uodKeywordFilter;
	} 
}