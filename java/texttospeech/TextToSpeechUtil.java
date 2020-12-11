package com.texttospeech;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * 2020/12/11 11:31
 * 文字转语音
 * https://github.com/freemansoft/jacob-project
 * <dependency>
 * <groupId>com.hynnet</groupId>
 * <artifactId>jacob</artifactId>
 * <version>1.20</version>
 * <scope>system</scope>
 * <systemPath>${basedir}/src/test/java/com/jacob/jacob.jar</systemPath>
 * </dependency>
 * <p>
 * 将相应版本的dll文件放在java.exe路径下
 *
 * @author owen pan
 */
public class TextToSpeechUtil {
    /**
     * Enum SpeechAudioFormatType
     *     SAFTDefault = -1
     *     SAFTNoAssignedFormat = 0
     *     SAFTText = 1
     *     SAFTNonStandardFormat = 2
     *     SAFTExtendedAudioFormat = 3
     *
     *     // Standard PCM wave formats
     *     SAFT8kHz8BitMono = 4
     *     SAFT8kHz8BitStereo = 5
     *     SAFT8kHz16BitMono = 6
     *     SAFT8kHz16BitStereo = 7
     *     SAFT11kHz8BitMono = 8
     *     SAFT11kHz8BitStereo = 9
     *     SAFT11kHz16BitMono = 10
     *     SAFT11kHz16BitStereo = 11
     *     SAFT12kHz8BitMono = 12
     *     SAFT12kHz8BitStereo = 13
     *     SAFT12kHz16BitMono = 14
     *     SAFT12kHz16BitStereo = 15
     *     SAFT16kHz8BitMono = 16
     *     SAFT16kHz8BitStereo = 17
     *     SAFT16kHz16BitMono = 18
     *     SAFT16kHz16BitStereo = 19
     *     SAFT22kHz8BitMono = 20
     *     SAFT22kHz8BitStereo = 21
     *     SAFT22kHz16BitMono = 22
     *     SAFT22kHz16BitStereo = 23
     *     SAFT24kHz8BitMono = 24
     *     SAFT24kHz8BitStereo = 25
     *     SAFT24kHz16BitMono = 26
     *     SAFT24kHz16BitStereo = 27
     *     SAFT32kHz8BitMono = 28
     *     SAFT32kHz8BitStereo = 29
     *     SAFT32kHz16BitMono = 30
     *     SAFT32kHz16BitStereo = 31
     *     SAFT44kHz8BitMono = 32
     *     SAFT44kHz8BitStereo = 33
     *     SAFT44kHz16BitMono = 34
     *     SAFT44kHz16BitStereo = 35
     *     SAFT48kHz8BitMono = 36
     *     SAFT48kHz8BitStereo = 37
     *     SAFT48kHz16BitMono = 38
     *     SAFT48kHz16BitStereo = 39
     *
     *     // TrueSpeech format
     *     SAFTTrueSpeech_8kHz1BitMono = 40
     *
     *     // A-Law formats
     *     SAFTCCITT_ALaw_8kHzMono = 41
     *     SAFTCCITT_ALaw_8kHzStereo = 42
     *     SAFTCCITT_ALaw_11kHzMono = 43
     *     SAFTCCITT_ALaw_11kHzStereo = 4
     *     SAFTCCITT_ALaw_22kHzMono = 44
     *     SAFTCCITT_ALaw_22kHzStereo = 45
     *     SAFTCCITT_ALaw_44kHzMono = 46
     *     SAFTCCITT_ALaw_44kHzStereo = 47
     *
     *     // u-Law formats
     *     SAFTCCITT_uLaw_8kHzMono = 48
     *     SAFTCCITT_uLaw_8kHzStereo = 49
     *     SAFTCCITT_uLaw_11kHzMono = 50
     *     SAFTCCITT_uLaw_11kHzStereo = 51
     *     SAFTCCITT_uLaw_22kHzMono = 52
     *     SAFTCCITT_uLaw_22kHzStereo = 53
     *     SAFTCCITT_uLaw_44kHzMono = 54
     *     SAFTCCITT_uLaw_44kHzStereo = 55
     *     SAFTADPCM_8kHzMono = 56
     *     SAFTADPCM_8kHzStereo = 57
     *     SAFTADPCM_11kHzMono = 58
     *     SAFTADPCM_11kHzStereo = 59
     *     SAFTADPCM_22kHzMono = 60
     *     SAFTADPCM_22kHzStereo = 61
     *     SAFTADPCM_44kHzMono = 62
     *     SAFTADPCM_44kHzStereo = 63
     *
     *     // GSM 6.10 formats
     *     SAFTGSM610_8kHzMono = 64
     *     SAFTGSM610_11kHzMono = 65
     *     SAFTGSM610_22kHzMono = 66
     *     SAFTGSM610_44kHzMono = 67
     *
     *     // Other formats
     *     SAFTNUM_FORMATS = 68
     * End Enum
     */
    /**
     * Enum SpeechStreamFileMode
     * SSFMOpenForRead = 0
     * [hidden] SSFMOpenReadWrite = 1
     * [hidden] SSFMCreate = 2
     * SSFMCreateForWrite = 3
     * End Enum
     */

    public static boolean transfer(String text, String targetFilePath) {
        ActiveXComponent ax = null;
        try {
            ax = new ActiveXComponent("Sapi.SpVoice");
            // 运行时输出语音内容
            Dispatch spVoice = ax.getObject();
            // 音量 0-100
            ax.setProperty("Volume", new Variant(100));
            // 语音朗读速度 -10 到 +10
            ax.setProperty("Rate", new Variant(-2));
            // 执行朗读
            Dispatch.call(spVoice, "Speak", new Variant(text));

            /*
             * ===================================
             * 以上是朗读部分，以下是生成音频文件部分
             * =================================
             */

            ax = new ActiveXComponent("Sapi.SpFileStream");
            Dispatch spFileStream = ax.getObject();

            ax = new ActiveXComponent("Sapi.SpAudioFormat");
            Dispatch spAudioFormat = ax.getObject();

            // 设置音频流格式,SpeechAudioFormatType
            Dispatch.put(spAudioFormat, "Type", new Variant(22));
            // 设置文件输出流格式
            Dispatch.putRef(spFileStream, "Format", spAudioFormat);
            // 调用输出 文件流打开方法，创建一个.wav文件
            Dispatch.call(spFileStream, "Open",
                    // 文件路径
                    new Variant(targetFilePath),
                    //文件模式，SpeechStreamFileMode
                    new Variant(3),
                    //DoEvents,When FileMode is SSFMCreateForWrite, DoEvents specifies whether playback of the resulting sound file will generate voice events. Default value is False.
                    new Variant(true)
            );
            // 设置声音对象的音频输出流为输出文件对象
            Dispatch.putRef(spVoice, "AudioOutputStream", spFileStream);
            // 设置音量 0到100
            Dispatch.put(spVoice, "Volume", new Variant(100));
            // 设置朗读速度,-10到10
            Dispatch.put(spVoice, "Rate", new Variant(-2));
            // 开始朗读
            Dispatch.call(spVoice, "Speak", new Variant(text));

            // 关闭输出文件
            Dispatch.call(spFileStream, "Close");
            Dispatch.putRef(spVoice, "AudioOutputStream", null);

            spAudioFormat.safeRelease();
            spFileStream.safeRelease();
            spVoice.safeRelease();
            ax.safeRelease();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        TextToSpeechUtil.transfer("你好，小吴，哈哈哈哈哈", "d:/test.mp3");
    }
}
