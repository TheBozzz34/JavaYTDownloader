package Downloader;

import io.sentry.Sentry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Exception;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] args)  {

        Sentry.init(options -> {
            options.setDsn("https://949b969343314271be199caba5dd897a@o561860.ingest.sentry.io/6510977");
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.setTracesSampleRate(1.0);
            // When first trying Sentry it's good to see what the SDK is doing:
            options.setDebug(false);
        });

        Logger logger = LoggerFactory.getLogger(Main.class);
        logger.info("Initialized Sentry");


        try {
            String[] command =
                    {
                            "yt-dlp",
                    };
            Process p = Runtime.getRuntime().exec(command);
            new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
            new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
            PrintWriter stdin = new PrintWriter(p.getOutputStream());
            stdin.println("yt-dlp");
            // Holy Fucking Shit I hate this goddamn code
            stdin.close();
            int returnCode = p.waitFor();
            System.out.println("Return code = " + returnCode);

        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }
}
class SyncPipe implements Runnable
{
    public SyncPipe(InputStream istrm, OutputStream ostrm) {
        istrm_ = istrm;
        ostrm_ = ostrm;
    }
    public void run() {
        try
        {
            final byte[] buffer = new byte[1024];
            for (int length = 0; (length = istrm_.read(buffer)) != -1; )
            {
                ostrm_.write(buffer, 0, length);
            }
        }
        catch (Exception e)
        {
            Sentry.captureException(e);
        }
    }
    private final OutputStream ostrm_;
    private final InputStream istrm_;
}
