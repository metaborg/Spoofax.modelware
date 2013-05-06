package org.spoofax.modelware.gmf.benchmarking;

import org.spoofax.modelware.gmf.EditorPairEvent;
import org.spoofax.modelware.gmf.EditorPairObserver;

public class SpoofaxModelwareBenchmarker implements EditorPairObserver {

	private long parsing;
	private long term2model;
	private long compare;
	private long merge;
	private long render;
	private long model2term;
	private long layoutPreservation;
	private long total1;
	private long total2;
	
	@Override
	public void notify(EditorPairEvent event) {
		switch (event) {
		
		case PreParse:
			System.out.println("Start parsing");
			parsing = System.currentTimeMillis();
			total1 = parsing;
			break;
		case PostParse:
			parsing = System.currentTimeMillis() - parsing;
			break;

		case PreTerm2Model:
			term2model = System.currentTimeMillis();
			break;
		case PostTerm2Model:
			term2model = System.currentTimeMillis() - term2model;
			break;
			
		case PreCompare:
			compare = System.currentTimeMillis();
			break;
		case PostCompare:
			compare = System.currentTimeMillis() - compare;
			break;
			
		case PreMerge:
			merge = System.currentTimeMillis();
			break;
		case PostMerge:
			merge = System.currentTimeMillis() - merge;
			break;
			
		case PreRender:
			render = System.currentTimeMillis();
			break;
		case PostRender:
			render = System.currentTimeMillis() - render;
			total1 = System.currentTimeMillis() - total1;
			printText2model();
			break;
			
			
		case PreModel2Term:
			model2term = System.currentTimeMillis();
			total2 = model2term;
			break;
		case PostModel2Term:
			model2term = System.currentTimeMillis() - model2term;
			break;
			
			
		case PreLayoutPreservation:
			layoutPreservation = System.currentTimeMillis();
			break;
		case PostLayoutPreservation:
			layoutPreservation = System.currentTimeMillis() - layoutPreservation;
			total2 = System.currentTimeMillis() - total2;
			printModel2Text();
			break;
		
			
		default:
			break;
		}
	}
	
	private void printText2model() {
		System.out.println();
		System.out.println("parsing:\t" + parsing);
		System.out.println("term2model:\t" + term2model);
		System.out.println("compare:\t" + compare);
		System.out.println("merge:\t\t" + merge);
		System.out.println("render:\t\t" + render);
		//System.out.println("real total:\t" + total1);
		long calcTotal = parsing + term2model + compare + merge + render;
		//System.out.println("calc. total:\t" + calcTotal);
		System.out.println();
	}
	
	private void printModel2Text() {
		System.out.println();
		System.out.println("model2term:\t" + model2term);
		System.out.println("layoutPres.:\t" + layoutPreservation);	
		//System.out.println("real total:\t" + total2);
		long calcTotal = model2term + layoutPreservation;
		//System.out.println("calc. total:\t" + calcTotal);
		System.out.println();
	}
}
