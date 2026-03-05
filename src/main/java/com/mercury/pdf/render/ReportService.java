package com.mercury.pdf.render;

/**
 * Backward-compatible alias for {@link PdfRenderService}.
 *
 * <p>This class exists so that existing code using {@code ReportService}
 * continues to compile and run after the rename to {@code PdfRenderService}.
 *
 * @deprecated Use {@link PdfRenderService} instead. This class will be removed in a future version.
 */
@Deprecated
public class ReportService extends PdfRenderService {
}
