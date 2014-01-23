/*
 * $Header$
 * $Revision: 1283 $
 * $Date: 2007-01-06 16:48:58 +0100 (Sat, 06 Jan 2007) $
 *
 * ====================================================================
 *
 * Copyright 2007 Ryan Gustafson
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 * 
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 * 
 *   * Neither the name of the Jaxen Project nor the names of its
 *     contributors may be used to endorse or promote products derived 
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * This software consists of voluntary contributions made by many 
 * individuals on behalf of the Jaxen Project and was originally 
 * created by bob mcwhirter <bob@werken.com> and 
 * James Strachan <jstrachan@apache.org>.  For more information on the 
 * Jaxen Project, please see <http://www.jaxen.org/>.
 * 
 * $Id: ExprComparator.java 1283 2007-01-06 15:48:58Z elharo $
 */

package org.jaxen.test;

import java.util.Comparator;
import java.util.List;

import org.jaxen.expr.AdditiveExpr;
import org.jaxen.expr.AllNodeStep;
import org.jaxen.expr.CommentNodeStep;
import org.jaxen.expr.EqualityExpr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LiteralExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.LogicalExpr;
import org.jaxen.expr.MultiplicativeExpr;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.ProcessingInstructionNodeStep;
import org.jaxen.expr.RelationalExpr;
import org.jaxen.expr.TextNodeStep;
import org.jaxen.expr.UnaryExpr;
import org.jaxen.expr.UnionExpr;
import org.jaxen.expr.VariableReferenceExpr;


class ExprComparator implements Comparator {

    public static final Comparator EXPR_COMPARATOR = new ExprComparator();

	private static final int TYPE_ADDITIVE_EXPR = 1;
	private static final int TYPE_ALL_NODE_STEP = 2;
	private static final int TYPE_COMMENT_NODE_STEP = 3;
	private static final int TYPE_EQUALITY_EXPR = 4;
	private static final int TYPE_FILTER_EXPR = 5;
	private static final int TYPE_FUNCTION_CALL_EXPR = 6;
	private static final int TYPE_LITERAL_EXPR = 7;
	private static final int TYPE_LOCATION_PATH = 8;
	private static final int TYPE_LOGICAL_EXP = 9;
	private static final int TYPE_MULTIPLICATIVE_EXPR = 10;
	private static final int TYPE_NAME_STEP = 11;
	private static final int TYPE_NUMBER_EXPR = 12;
	private static final int TYPE_PATH_EXPR = 13;
	private static final int TYPE_PREDICATE = 14;
	private static final int TYPE_PROCESSING_INSTRUCTION_NODE_STEP = 15;
	private static final int TYPE_RELATIONAL_EXPR = 16;
	private static final int TYPE_TEXT_NODE_STEP = 17;
	private static final int TYPE_UNARY_EXPR = 18;
	private static final int TYPE_UNION_EXPR = 19;
	private static final int TYPE_VARIABLE_REFERENCE_EXPR = 20;

	private ExprComparator()
	{
	}

	public int compare(Object o1, Object o2)
	{
		int type1 = getType(o1);
		int type2 = getType(o2);

		int cmp;
		if (type1 == type2)
		{
			switch (type1)
			{
				case TYPE_ADDITIVE_EXPR:
					AdditiveExpr additiveExpr1 = (AdditiveExpr)o1;
					AdditiveExpr additiveExpr2 = (AdditiveExpr)o2;
					cmp = additiveExpr1.getOperator().compareTo(additiveExpr2.getOperator());
					if (cmp == 0)
					{
						cmp = compare(additiveExpr1.getLHS(), additiveExpr2.getLHS());
						if (cmp == 0)
						{
							cmp = compare(additiveExpr1.getRHS(), additiveExpr2.getRHS());
						}
					}
					break;
				case TYPE_ALL_NODE_STEP:
					AllNodeStep allNodeStep1 = (AllNodeStep)o1;
					AllNodeStep allNodeStep2 = (AllNodeStep)o2;
					cmp = allNodeStep1.getAxis() - allNodeStep2.getAxis();
					if (cmp == 0)
					{
						cmp = compareLists(allNodeStep1.getPredicates(), allNodeStep2.getPredicates());
					}
					break;
				case TYPE_COMMENT_NODE_STEP:
					CommentNodeStep commentNodeStep1 = (CommentNodeStep)o1;
					CommentNodeStep commentNodeStep2 = (CommentNodeStep)o2;
					cmp = commentNodeStep1.getAxis() - commentNodeStep2.getAxis();
					if (cmp == 0)
					{
						cmp = compareLists(commentNodeStep1.getPredicates(), commentNodeStep2.getPredicates());
					}
					break;
				case TYPE_EQUALITY_EXPR:
					EqualityExpr equalityExpr1 = (EqualityExpr)o1;
					EqualityExpr equalityExpr2 = (EqualityExpr)o2;
					cmp = equalityExpr1.getOperator().compareTo(equalityExpr2.getOperator());
					if (cmp == 0)
					{
						cmp = compare(equalityExpr1.getLHS(), equalityExpr1.getLHS());
						if (cmp == 0)
						{
							cmp = compare(equalityExpr1.getRHS(), equalityExpr1.getRHS());
						}
					}
					break;
				case TYPE_FILTER_EXPR:
					if (true)
						throw new RuntimeException("Not yet implemented!");
					break;
				case TYPE_FUNCTION_CALL_EXPR:
					FunctionCallExpr functionCallExpr1 = (FunctionCallExpr)o1;
					FunctionCallExpr functionCallExpr2 = (FunctionCallExpr)o2;
					cmp = compareStrings(functionCallExpr1.getPrefix(), functionCallExpr2.getPrefix());
					if (cmp == 0)
					{
						cmp = functionCallExpr1.getFunctionName().compareTo(functionCallExpr2.getFunctionName());
						if (cmp == 0)
						{
							cmp = compareLists(functionCallExpr1.getParameters(), functionCallExpr2.getParameters());
						}
					}
					break;
				case TYPE_LITERAL_EXPR:
					LiteralExpr literalExpr1 = (LiteralExpr)o1;
					LiteralExpr literalExpr2 = (LiteralExpr)o2;
					cmp = literalExpr1.getLiteral().compareTo(literalExpr2.getLiteral());
					break;
				case TYPE_LOCATION_PATH:
					LocationPath locationPath1 = (LocationPath)o1;
					LocationPath locationPath2 = (LocationPath)o2;
					if (locationPath1.isAbsolute() == locationPath2.isAbsolute())
					{
						cmp = compareLists(locationPath1.getSteps(), locationPath2.getSteps());
					}
					else if (locationPath1.isAbsolute())
					{
						cmp = 1;
					}
					else
					{
						cmp = -1;
					}
					break;
				case TYPE_LOGICAL_EXP:
					LogicalExpr logicalExpr1 = (LogicalExpr)o1;
					LogicalExpr logicalExpr2 = (LogicalExpr)o2;
					cmp = logicalExpr1.getOperator().compareTo(logicalExpr2.getOperator());
					if (cmp == 0)
					{
						cmp = compare(logicalExpr1.getLHS(), logicalExpr2.getLHS());
						if (cmp == 0)
						{
							cmp = compare(logicalExpr1.getRHS(), logicalExpr2.getRHS());
						}
					}
					break;
				case TYPE_MULTIPLICATIVE_EXPR:
					MultiplicativeExpr multiplicativeExpr1 = (MultiplicativeExpr)o1;
					MultiplicativeExpr multiplicativeExpr2 = (MultiplicativeExpr)o2;
					cmp = multiplicativeExpr1.getOperator().compareTo(multiplicativeExpr2.getOperator());
					if (cmp == 0)
					{
						cmp = compare(multiplicativeExpr1.getLHS(), multiplicativeExpr2.getLHS());
						if (cmp == 0)
						{
							cmp = compare(multiplicativeExpr1.getRHS(), multiplicativeExpr2.getRHS());
						}
					}
					break;
				case TYPE_NAME_STEP:
					NameStep nameStep1 = (NameStep)o1;
					NameStep nameStep2 = (NameStep)o2;
					cmp = nameStep1.getAxis() - nameStep2.getAxis();
					if (cmp == 0)
					{
						cmp = compareStrings(nameStep1.getPrefix(), nameStep2.getPrefix());

						if (cmp == 0)
						{
							cmp = nameStep1.getLocalName().compareTo(nameStep2.getLocalName());
							if (cmp == 0)
							{
								cmp = compareLists(nameStep1.getPredicates(), nameStep2.getPredicates());
							}
						}
					}
					break;
				case TYPE_NUMBER_EXPR:
					NumberExpr numberExpr1 = (NumberExpr)o1;
					NumberExpr numberExpr2 = (NumberExpr)o2;
					cmp = new Double(numberExpr1.getNumber().doubleValue()).compareTo(new Double(numberExpr2.getNumber().doubleValue()));
					break;
				case TYPE_PATH_EXPR:
					PathExpr pathExpr1 = (PathExpr)o1;
					PathExpr pathExpr2 = (PathExpr)o2;
					cmp = compare(pathExpr1.getLocationPath(), pathExpr2.getLocationPath());
					if (cmp == 0)
					{
						cmp = compare(pathExpr1.getFilterExpr(), pathExpr2.getFilterExpr());
					}
					break;
				case TYPE_PREDICATE:
					Predicate predicate1 = (Predicate)o1;
					Predicate predicate2 = (Predicate)o2;
					cmp = compare(predicate1.getExpr(), predicate2.getExpr());
					break;
				case TYPE_PROCESSING_INSTRUCTION_NODE_STEP:
					ProcessingInstructionNodeStep processingInstructionNodeStep1 = (ProcessingInstructionNodeStep)o1;
					ProcessingInstructionNodeStep processingInstructionNodeStep2 = (ProcessingInstructionNodeStep)o2;
					cmp = processingInstructionNodeStep1.getAxis() - processingInstructionNodeStep2.getAxis();
					if (cmp == 0)
					{
						cmp = compareStrings(processingInstructionNodeStep1.getName(), processingInstructionNodeStep2.getName());
						if (cmp == 0)
						{
							cmp = compareLists(processingInstructionNodeStep1.getPredicates(), processingInstructionNodeStep2.getPredicates());
						}
					}
					break;
				case TYPE_RELATIONAL_EXPR:
					RelationalExpr relationalExpr1 = (RelationalExpr)o1;
					RelationalExpr relationalExpr2 = (RelationalExpr)o2;
					cmp = relationalExpr1.getOperator().compareTo(relationalExpr2.getOperator());
					if (cmp == 0)
					{
						cmp = compare(relationalExpr1.getLHS(), relationalExpr2.getLHS());
						if (cmp == 0)
						{
							cmp = compare(relationalExpr1.getRHS(), relationalExpr2.getRHS());
						}
					}
					break;
				case TYPE_TEXT_NODE_STEP:
					TextNodeStep textNodeStep1 = (TextNodeStep)o1;
					TextNodeStep textNodeStep2 = (TextNodeStep)o2;
					cmp = textNodeStep1.getAxis() - textNodeStep2.getAxis();
					if (cmp == 0)
					{
						cmp = compareLists(textNodeStep1.getPredicates(), textNodeStep2.getPredicates());
					}
					break;
				case TYPE_UNARY_EXPR:
					UnaryExpr unaryExpr1 = (UnaryExpr)o1;
					UnaryExpr unaryExpr2 = (UnaryExpr)o2;
					cmp = compare(unaryExpr1.getExpr(), unaryExpr2.getExpr());
					break;
				case TYPE_UNION_EXPR:
					if (true)
						throw new RuntimeException("Not yet implemented!");
					break;
				case TYPE_VARIABLE_REFERENCE_EXPR:
					VariableReferenceExpr variableReferenceExpr1 = (VariableReferenceExpr)o1;
					VariableReferenceExpr variableReferenceExpr2 = (VariableReferenceExpr)o2;
					cmp = compareStrings(variableReferenceExpr1.getPrefix(), variableReferenceExpr2.getPrefix());
					if (cmp == 0)
					{
						cmp = variableReferenceExpr1.getVariableName().compareTo(variableReferenceExpr2.getVariableName());
					}
					break;
				default:
					throw new IllegalArgumentException("Unhandled type: " + type1);
			}
		}
		else
		{
			cmp = type1 - type2;
		}
		return cmp;
	}

	private int compareStrings(String s1, String s2)
	{
		int cmp;
		if (s1 == s2)
		{
			cmp = 0;
		}
		else if (s1 == null)
		{
			cmp = -1;
		}
		else if (s2 == null)
		{
			cmp = 1;
		}
		else
		{
			cmp = s1.compareTo(s2);
		}
		return cmp;
	}

	private int compareLists(List list1, List list2)
	{
		int cmp;
		if (list1 == list2)
		{
			cmp = 0;
		}
		else if (list1 == null)
		{
			cmp = -1;
		}
		else if (list2 == null)
		{
			cmp = 1;
		}
		else
		{
			cmp = list1.size() - list2.size();
			if (cmp == 0)
			{
				for (int i = 0; i < list1.size() && cmp == 0; i++)
				{
					cmp = compare(list1.get(i), list2.get(i));
				}
			}
		}
		return cmp;
	}

	private int getType(Object node)
	{
		if (node instanceof AdditiveExpr)
		{
			return TYPE_ADDITIVE_EXPR;
		}
		else if (node instanceof AllNodeStep)
		{
			return TYPE_ALL_NODE_STEP;
		}
		else if (node instanceof CommentNodeStep)
		{
			return TYPE_COMMENT_NODE_STEP;
		}
		else if (node instanceof EqualityExpr)
		{
			return TYPE_EQUALITY_EXPR;
		}
		else if (node instanceof FilterExpr)
		{
			return TYPE_FILTER_EXPR;
		}
		else if (node instanceof FunctionCallExpr)
		{
			return TYPE_FUNCTION_CALL_EXPR;
		}
		else if (node instanceof LiteralExpr)
		{
			return TYPE_LITERAL_EXPR;
		}
		else if (node instanceof LocationPath)
		{
			return TYPE_LOCATION_PATH;
		}
		else if (node instanceof LogicalExpr)
		{
			return TYPE_LOGICAL_EXP;
		}
		else if (node instanceof MultiplicativeExpr)
		{
			return TYPE_MULTIPLICATIVE_EXPR;
		}
		else if (node instanceof NameStep)
		{
			return TYPE_NAME_STEP;
		}
		else if (node instanceof NumberExpr)
		{
			return TYPE_NUMBER_EXPR;
		}
		else if (node instanceof PathExpr)
		{
			return TYPE_PATH_EXPR;
		}
		else if (node instanceof Predicate)
		{
			return TYPE_PREDICATE;
		}
		else if (node instanceof ProcessingInstructionNodeStep)
		{
			return TYPE_PROCESSING_INSTRUCTION_NODE_STEP;
		}
		else if (node instanceof RelationalExpr)
		{
			return TYPE_RELATIONAL_EXPR;
		}
		else if (node instanceof TextNodeStep)
		{
			return TYPE_TEXT_NODE_STEP;
		}
		else if (node instanceof UnaryExpr)
		{
			return TYPE_UNARY_EXPR;
		}
		else if (node instanceof UnionExpr)
		{
			return TYPE_UNION_EXPR;
		}
		else if (node instanceof VariableReferenceExpr)
		{
			return TYPE_VARIABLE_REFERENCE_EXPR;
		}
		else
		{
			throw new IllegalArgumentException("Unknown Jaxen AST node type: " + node);
		}
	}
}
