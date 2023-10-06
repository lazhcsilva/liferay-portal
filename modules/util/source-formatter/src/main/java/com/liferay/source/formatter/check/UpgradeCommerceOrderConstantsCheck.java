/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.liferay.source.formatter.check;

import com.liferay.petra.string.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lázaro Costa
 */
public class UpgradeCommerceOrderConstantsCheck
	extends BaseUpgradeMatcherReplacementCheck {

	@Override
	protected String afterFormat(
		String fileName, String absolutePath, String content,
		String newContent) {

		if (fileName.endsWith(".java")) {
			newContent = addNewImports(newContent);
		}

		return newContent;
	}

	@Override
	protected String formatMatcherIteration(
		String content, String newContent, Matcher matcher) {

		String constant = matcher.group(1);
		String constantCall = matcher.group();
		String newConstantCall = "CommerceOrderPaymentConstants.";

		if (constant.equals("STATUS_AUTHORIZED")) {
			newConstantCall = newConstantCall + "STATUS_AUTHORIZED";
		}
		else if (constant.equals("STATUS_PAID")) {
			newConstantCall = newConstantCall + "STATUS_COMPLETED";
		}
		else if (constant.equals("STATUS_PENDING")) {
			newConstantCall = newConstantCall + "STATUS_PENDING";
		}

		return StringUtil.replace(newContent, constantCall, newConstantCall);
	}

	@Override
	protected String[] getNewImports() {
		return new String[] {
			"com.liferay.commerce.constants.CommerceOrderPaymentConstants"
		};
	}

	@Override
	protected Pattern getPattern() {
		return Pattern.compile("CommerceOrderConstants.PAYMENT_([A-Z_]+)");
	}

	@Override
	protected String[] getValidExtensions() {
		return new String[] {"java", "jsp"};
	}

}