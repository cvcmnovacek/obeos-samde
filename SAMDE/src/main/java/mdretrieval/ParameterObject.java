//------------------------------------------------------------------------------
//
// Project: OBEOS METADATA EDITOR
// Authors: Natascha Neumaerker, Siemens Convergence Creators, Prague (CZ)
//          Milan Novacek, Siemens Convergence Creators, Prague (CZ)
//          Radim Zajonc, Siemens Convergence Creators, Prague (CZ)
//          
//------------------------------------------------------------------------------
// Copyright (C) 2017 Siemens Convergence Creators, Prague (CZ)
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies of this Software or works derived from this Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//------------------------------------------------------------------------------

package mdretrieval;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import global.GUIrefs;
import utilities.StringUtilities;

public class ParameterObject {
	String name;
	String placeholder;
	String displayLabel;
	String value = "";
	// if ParameterObject is not associated with options list
	String textValue = "";
	String selEnumKey = "";
	String queryContribution = "";

	// these attributes are optional and normally(?)
	// mutually exclusive with the existence of an explicit options list
	String pattern;
	Integer minInclusive;
	Integer maxInclusive;

	// label -> value
	Map<String, String> options = null;

	public void initParams(String name, String placeholder, String displayLabel, String pattern, Integer minInclusive,
			Integer maxInclusive) {

		this.name = name;
		this.placeholder = placeholder;
		this.displayLabel = displayLabel;
		this.pattern = pattern;
		this.minInclusive = minInclusive;
		this.maxInclusive = maxInclusive;
	}

	public boolean hasOptions() {
		return ((null != options) && options.size() > 0);
	}

	public void initOptionsMap() {
		options = new HashMap<String, String>();
	}

	public void addOption(String label, String value) {
		options.put(label, value);
	}

	public String toString() {
		StringBuilder builder = new StringBuilder("ParameterObject: ");
		builder.append(name + "; ");
		builder.append(placeholder + "\n");
		builder.append(value + "\n");
		builder.append(textValue + "\n");
		builder.append(pattern + "\n");
		builder.append(minInclusive + "; ");
		builder.append(maxInclusive + "; ");
		if (null != options) {
			for (Map.Entry<String, String> e : options.entrySet()) {
				builder.append(e.getKey() + "," + e.getValue() + "\n");
			}
		} else {
			builder.append("\n no options available\n");
		}
		return builder.toString();
	}

	// setters and getters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public String getSelEnumKey() {
		return selEnumKey;
	}

	public void setSelEnumKey(String enumKey) {
		this.selEnumKey = enumKey;
		this.displayLabel = enumKey;
		if (null != options) {
			this.value = options.get(enumKey);
		}
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		if (this.pattern.equals("")) {
			this.pattern = ".*";
		}
	}

	public Integer getMinInclusive() {
		return minInclusive;
	}

	public void setMinInclusive(Integer minInclusive) {
		this.minInclusive = minInclusive;
	}

	public Integer getMaxInclusive() {
		return maxInclusive;
	}

	public void setMaxInclusive(Integer maxInclusive) {
		this.maxInclusive = maxInclusive;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;

	}

	public String getTextValue() {
		return textValue;
	}

	public void setTextValue(String textValue) {
		boolean valid = true;
		String message="";
		if (null != minInclusive) {
			int a;
			try {
				a = Integer.parseInt(textValue);
				if ( (a < minInclusive.intValue()) || ( (maxInclusive != null) && (a > maxInclusive.intValue()) ) ) {
					valid = false;
					message = "Value is outside allowed range";
				}
			} catch (NumberFormatException e) {
				message = "Value is not an integer";
			}
		}

		if (!StringUtilities.isEmpty(pattern)) {
			if (!Pattern.matches(this.pattern, textValue)) {
				valid = false;
				message = "Input does not match regexp " + pattern;
			}
		}
		if (valid) {
			this.textValue = textValue;
			this.displayLabel = textValue;
		}
		else{
			GUIrefs.displayAlert("Input error: \\n" + StringUtilities.escapeQuotes(message));
		}
	}

	public String getQueryContribution() {
		return queryContribution;
	}

	public void setQueryContribution(String queryContribution) {
		this.queryContribution = queryContribution;
	}

	public Map<String, String> getOptions() {
		return options;
	}

	public void setOptions(Map<String, String> options) {
		this.options = options;
	}

}
