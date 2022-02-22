import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CLViewToPdfHelper
{

    private static File createPdfFile() throws IOException
    {
        String sTimeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
        return createPdfFile("PDF" + sTimeStamp + ".pdf");
    }

    private static File createPdfFile(String sFileName) throws IOException
    {
        String sDownloadPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/download";
        File clFile = null;
        if (sFileName != null && sFileName.contains(".pdf"))
        {
            clFile = new File(sDownloadPath + "/" + sFileName);
        }
        return clFile;
    }


    private static void sharePdfIntent(Context clContext, URI uriToImage)
    {
        Intent clShareIntent = new Intent();
        clShareIntent.setAction(Intent.ACTION_SEND);
        clShareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
        clShareIntent.setType("application/pdf");
        clContext.startActivity(Intent.createChooser(clShareIntent, null));
    }

    private static File generateViewToPdf(Context clContext, View clView)
    {
        File clFile = null;

        if (clView != null)
        {
            Bitmap clBitmap = Bitmap.createBitmap(clView.getWidth(), clView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas clCanvas = new Canvas(clBitmap);
            clView.draw(clCanvas);

            WindowManager clManager = (WindowManager) clContext.getSystemService(clContext.WINDOW_SERVICE);
            //  Display display = clManager.getDefaultDisplay();
            DisplayMetrics clDisplayMetrics = new DisplayMetrics();
            ((Activity) clContext).getWindowManager().getDefaultDisplay().getMetrics(clDisplayMetrics);
            float fHeight = clDisplayMetrics.heightPixels;
            float fWidth = clDisplayMetrics.widthPixels;

            int iConvertHeight = (int) fHeight, iConvertWidth = (int) fWidth;

            FileOutputStream clFOStream = null;
            PdfDocument clPdfDocument = new PdfDocument();
            PdfDocument.PageInfo clPageInfo = new PdfDocument.PageInfo.Builder(iConvertWidth, iConvertHeight, 1).create();
            PdfDocument.Page page = clPdfDocument.startPage(clPageInfo);

            Canvas clPageCanvas = page.getCanvas();

            Paint clPaint = new Paint();
            clPageCanvas.drawPaint(clPaint);

            clBitmap = Bitmap.createScaledBitmap(clBitmap, iConvertWidth, iConvertHeight, true);

            clPaint.setColor(Color.BLUE);
            clPageCanvas.drawBitmap(clBitmap, 0, 0, null);
            clPdfDocument.finishPage(page);

            try
            {
                clFile = createPdfFile();
                clFOStream = new FileOutputStream(createPdfFile());
                clPdfDocument.writeTo(clFOStream);
                clFOStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(clContext, "Something went wrong: " + e.toString(), Toast.LENGTH_LONG).show();
            }

            // close the document
            clPdfDocument.close();

            Toast.makeText(clContext, "successfully pdf created", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(clContext, "View Cannot be Null", Toast.LENGTH_SHORT).show();
        }
        return clFile;
    }

    public static void shareViewAsPdf(Context clContext, View clView)
    {
        File clFile = generateViewToPdf(clContext, clView);

        ///// share  intent ////
        if (clFile != null)
        {
            sharePdfIntent(clContext, clFile.toURI());
        }
    }

    public static void viewAsPdf(Context clContext, View clView)
    {
        File clFile = generateViewToPdf(clContext, clView);
        /////// preview intent ////////
        if (clFile != null)
        {
            viewPdfIntent(clContext, clFile);
        }
    }

    private static void viewPdfIntent(Context clContext, File clFile)
    {
        if (clFile != null && clFile.exists())
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(clFile);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try
            {
                clContext.startActivity(intent);
            }
            catch (ActivityNotFoundException e)
            {
                Toast.makeText(clContext, "No Application for pdf view", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
