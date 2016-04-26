package com.example.anhle.kara;

/**
 * Created by anhle on 4/26/16.
 */
public class Config {

    public static final int YUNINFO_REQUEST_CODE_RECORD_VIDEO = 0x100;

    public static final int YUNINFO_REQUEST_CODE_PICK_CONTACT = 0x101;

    public static final int YUNINFO_REQUEST_CODE_PLAY_VIDEO = 0x102;

    public static final int YUNINFO_REQUEST_CODE_PICK_VIDEO = 0x103;

    public static final String YUNINFO_RESULT_DATA = "yuninfo_result_data";

    public static final String YUNINFO_EXTRA_URL = "yuninfo_url";

    public static final String YUNINFO_VIDEO_UPLOAD_URL = "http://42.120.19.149/videodemo/uploadFile/upLoadFile.php";

    public static final String YUNINFO_SEND_VIDEO_URL = "http://182.140.234.47/video/sendVideoMsg.do";

    public static final String YUNINFO_READ_VIDEO_URL = "http://182.140.234.47/video/acceptVideoMsg.do";

    public static final String YUNINFO_REQUEST_FORMAT = "json";

    public static final String YUNINFO_REQUEST_VERSION = "v1.0";

    public static final String YUNINFO_REQUEST_CLIENT_TYPE = "4";

    public static final int YUNINFO_MAX_VIDEO_DURATION = 300 * 1000;

    /************************* IDs Start *******************************/
    public static final int YUNINFO_ID_TASK_STARTED = 0x1001;
    public static final int YUNINFO_ID_TASK_CANCELED = 0x1002;
    public static final int YUNINFO_ID_TASK_PROGRESS = 0x1003;
    public static final int YUNINFO_ID_TASK_SUCCESSED = 0x1004;
    public static final int YUNINFO_ID_TASK_FAILED = 0x1005;

    public static final int YUNINFO_ID_TIME_COUNT = 0x1006;
    /************************* IDs End *************************/

}