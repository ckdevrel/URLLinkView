package com.takeoffandroid.urllinkview.library;

/**
 * Callback that is invoked with before and after the loading of a link preview
 * 
 */
public interface LinkViewCallback {

	void onBeforeLoading();

	/**
	 * 
	 * @param linkSourceContent
	 *            Class with all contents from preview.
	 * @param isNull
	 *            Indicates if the content is null.
	 */
	void onAfterLoading(LinkSourceContent linkSourceContent, boolean isNull);
}
