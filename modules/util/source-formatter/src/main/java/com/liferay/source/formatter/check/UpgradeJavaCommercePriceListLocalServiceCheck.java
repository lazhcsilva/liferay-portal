/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
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
public class UpgradeJavaCommercePriceListLocalServiceCheck
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
				content, javaMethodContent,
				_replaceMethod(newContent, fileName, javaMethodContent));
		}

		return newContent;
	}

	private boolean _checkMethodCall(
		String content, String fileContent, String methodCall) {

		String variableName = getVariableName(methodCall);

		if (!variableName.equals("CommercePriceListLocalService") &&
			!hasClassOrVariableName(
				"CommercePriceListLocalService", content, fileContent,
				methodCall)) {

			return false;
		}

		return true;
	}

	private String _reorderParameters(
		String methodCall, List<String> parameterList) {

		String parameters = JavaSourceUtil.getParameters(methodCall);

		String newParameter = StringBundler.concat(
			parameterList.get(1), StringPool.COMMA_AND_SPACE,
			parameterList.get(0));

		return StringUtil.replace(methodCall, parameters, newParameter);
	}

	private String _replaceMethod(
		String content, String fileName, String javaMethodContent) {

		Matcher matcher = _pattern.matcher(javaMethodContent);

		String newJavaMethodContent = javaMethodContent;

		while (matcher.find()) {
			int position = matcher.start();

			String methodCall = JavaSourceUtil.getMethodCall(
				javaMethodContent, position);

			List<String> parameterList = JavaSourceUtil.getParameterList(
				methodCall);

			if (!_checkMethodCall(content, javaMethodContent, methodCall)) {
				continue;
			}

			String message = StringBundler.concat(
				"Unable to format methods fetchByExternalReferenceCode from ",
				"CommercePriceListLocalService, Fill the new parameters ",
				"manually, see LPS-198414");

			String[] parameterTypes = {"long", "String"};

			if (!hasValidParameters(
					2, fileName, content, message, parameterList,
					parameterTypes)) {

				continue;
			}

			String newMethodCall = _reorderParameters(
				methodCall, parameterList);

			newJavaMethodContent = StringUtil.replace(
				javaMethodContent, methodCall, newMethodCall);
		}

		return newJavaMethodContent;
	}

	private static final Pattern _pattern = Pattern.compile(
		"(\\w+)\\.\\w*\\(?\\s*\\)?\\s*\\.?fetchByExternalReferenceCode\\(");

}