/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.string.StringUtil;
import com.liferay.source.formatter.check.util.JavaSourceUtil;
import com.liferay.source.formatter.parser.JavaClass;
import com.liferay.source.formatter.parser.JavaClassParser;
import com.liferay.source.formatter.parser.JavaMethod;
import com.liferay.source.formatter.parser.JavaTerm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lázaro Costa
 */
public class UpgradeJavaCommerceAccountGroupRelLocalServiceCheck
	extends BaseUpgradeCheck {

	@Override
	protected String format(
			String fileName, String absolutePath, String content)
		throws Exception {

		JavaClass javaClass = JavaClassParser.parseJavaClass(fileName, content);

		String newContent = content;

		for (JavaTerm childJavaTerm : javaClass.getChildJavaTerms()) {
			if (!childJavaTerm.isJavaMethod()) {
				continue;
			}

			JavaMethod javaMethod = (JavaMethod)childJavaTerm;

			String javaMethodContent = javaMethod.getContent();

			newContent = StringUtil.replace(
				newContent, javaMethodContent,
				_replaceCommerceAccountGroupRel(
					newContent, fileName, javaMethodContent));
		}

		return newContent;
	}

	private boolean _checkMethodCall(
		String content, String fileContent, String fileName,
		String methodCall) {

		String variableName = getVariableName(methodCall);

		if (!variableName.equals("AccountGroupRelLocalServiceUtil") &&
			!variableName.equals("AccountGroupRelServiceUtil") &&
			!hasClassOrVariableName(
				"AccountGroupRelLocalService", content, fileContent,
				methodCall) &&
			!hasClassOrVariableName(
				"AccountGroupRelService", content, fileContent, methodCall)) {

			return false;
		}

		String message = StringBundler.concat(
			"Unable to format methods addAccountGroupRel and ",
			"addCommerceAccountGroupRelfrom AccountGroupRelLocalService, ",
			"AccountGroupRelLocalServiceUtil, AccountGroupRelService, ",
			"AccountGroupRelServiceUtil. Format the new parameters manually, ",
			"see LPS-197142");

		List<String> parameterList = JavaSourceUtil.getParameterList(
			methodCall);

		String[] parameterTypes = {"String", "long", "long", "ServiceContext"};

		if (!hasValidParameters(
				4, fileName, content, message, parameterList, parameterTypes)) {

			return false;
		}

		return true;
	}

	private String _reorderParameters(
		String indent, String methodCall, List<String> parameterList) {

		String parameters = JavaSourceUtil.getParameters(methodCall);

		String newParameter = StringBundler.concat(
			StringPool.NEW_LINE, indent, StringPool.TAB, parameterList.get(2),
			StringPool.COMMA_AND_SPACE, parameterList.get(0),
			StringPool.COMMA_AND_SPACE, parameterList.get(1));

		return StringUtil.replace(methodCall, parameters, newParameter);
	}

	private String _replaceCommerceAccountGroupRel(
		String content, String fileName, String javaMethodContent) {

		Matcher matcher = _pattern.matcher(javaMethodContent);

		String newJavaMethodContent = javaMethodContent;

		while (matcher.find()) {
			int position = matcher.start();

			String methodCall = JavaSourceUtil.getMethodCall(
				javaMethodContent, position);

			List<String> parameterList = JavaSourceUtil.getParameterList(
				methodCall);

			if (!_checkMethodCall(
					content, javaMethodContent, fileName, methodCall)) {

				continue;
			}

			String newMethodCall = _reorderParameters(
				JavaSourceUtil.getIndent(
					JavaSourceUtil.getLine(
						javaMethodContent,
						JavaSourceUtil.getLineNumber(
							javaMethodContent, position))),
				methodCall, parameterList);

			newMethodCall = StringUtil.replace(
				newMethodCall, "addCommerceAccountGroupRel",
				"addAccountGroupRel");

			newJavaMethodContent = StringUtil.replace(
				newJavaMethodContent, methodCall, newMethodCall);
		}

		return newJavaMethodContent;
	}

	private static final Pattern _pattern = Pattern.compile(
		"\\w+\\.\\s*add(?:|Commerce)AccountGroupRel\\(\\s*.+(,\\s*.+)+\\)");

}