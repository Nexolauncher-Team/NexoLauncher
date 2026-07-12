package com.nexo.launcher.utils.stringutils;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Base64;
import android.widget.Toast;

import com.nexo.launcher.R;
import com.nexo.launcher.task.TaskExecutors;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    public static String insertSpace(Object prefixString, Object... suffixString) {
        return insertSpace(prefixString == null ? null : prefixString.toString(),
                Arrays.stream(suffixString).map(Object::toString).toArray(String[]::new));
    }

    /**
     * åœ¨å­—ç¬¦ä¸²ä¹‹é—´æ’å…¥ç©ºæ ¼
     *
     * @param prefixString ç¬¬ä¸€ä¸ªå­—ç¬¦ä¸²
     * @param suffixString ä¹‹åŽçš„å¤šä¸ªå­—ç¬¦ä¸²
     * @return è¿”å›žæ’å…¥å¥½ç©ºæ ¼çš„å­—ç¬¦ä¸² "string1 string2 string3"
     */
    public static String insertSpace(String prefixString, String... suffixString) {
        return insertString(" ", prefixString, suffixString);
    }

    public static String insertNewline(Object prefixString, Object... suffixString) {
        return insertNewline(prefixString == null ? null : prefixString.toString(),
                Arrays.stream(suffixString).map(Object::toString).toArray(String[]::new));
    }

    /**
     * åœ¨å­—ç¬¦ä¸²ä¹‹é—´æ’å…¥æ¢è¡Œç¬¦
     *
     * @param prefixString ç¬¬ä¸€ä¸ªå­—ç¬¦ä¸²
     * @param suffixString ä¹‹åŽçš„å¤šä¸ªå­—ç¬¦ä¸²
     * @return è¿”å›žæ’å…¥å¥½æ¢è¡Œç¬¦çš„å­—ç¬¦ä¸²
     */
    public static String insertNewline(String prefixString, String... suffixString) {
        return insertString("\r\n", prefixString, suffixString);
    }

    public static String insertString(String stringToInsert, String prefixString, String... suffixString) {
        StringJoiner stringJoiner = new StringJoiner(stringToInsert);
        if (prefixString != null) {
            stringJoiner.add(prefixString);
        }
        for (String string : suffixString) {
            stringJoiner.add(string);
        }

        return stringJoiner.toString();
    }

    public static String shiftString(String input, ShiftDirection direction, int shiftCount) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        //ç¡®ä¿ä½ç§»ä¸ªæ•°åœ¨å­—ç¬¦ä¸²é•¿åº¦èŒƒå›´å†…
        int length = input.length();
        shiftCount = shiftCount % length;
        if (shiftCount == 0) {
            return input;
        }

        switch (direction) {
            case LEFT:
                return input.substring(shiftCount) + input.substring(0, shiftCount);
            case RIGHT:
                return input.substring(length - shiftCount) + input.substring(0, length - shiftCount);
            default:
                throw new IllegalArgumentException("Invalid shift direction: " + direction);
        }
    }

    /**
     * @return æ£€æŸ¥å­—ç¬¦ä¸²æ˜¯å¦ä¸ºnullï¼Œå¦‚æžœæ˜¯é‚£ä¹ˆåˆ™è¿”å›ž""ï¼Œå¦‚æžœä¸æ˜¯ï¼Œåˆ™è¿”å›žå­—ç¬¦ä¸²æœ¬èº«
     */
    public static String getStringNotNull(String string) {
        if (string == null) return "";
        else return string;
    }

    /**
     * æ£€æŸ¥ä¸€æ®µå­—ç¬¦ä¸²å†…æ˜¯å¦å«æœ‰ä¸­æ–‡å­—ç¬¦ï¼ˆä¸­æ–‡æ ‡ç‚¹ï¼‰
     * @param str æ£€æŸ¥çš„å­—ç¬¦
     * @return æ˜¯å¦å¸¦æœ‰ä¸­æ–‡
     */
    public static boolean containsChinese(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile("[ä¸€-é¾¥|ï¼ï¼Œã€‚ï¼ˆï¼‰ã€Šã€‹â€œâ€ï¼Ÿï¼šï¼›ã€ã€‘]");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public static String formattingTime(String time) {
        int T = time.indexOf('T');
        int Z = time.indexOf('Z');
        if (T == -1 || Z == -1) return time;
        return StringUtils.insertSpace(time.substring(0, T), time.substring(T + 1, Z));
    }

    public static String formatDate(Date date, Locale locale, TimeZone timeZone) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", locale);
        formatter.setTimeZone(timeZone);
        return formatter.format(date);
    }

    public static String markdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);
    }

    public static void copyText(String label, String text, Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
        TaskExecutors.runInUIThread(() -> Toast.makeText(context, context.getString(R.string.generic_copied), Toast.LENGTH_SHORT).show());
    }

    public static String decodeBase64(String rawValue) {
        byte[] decodedBytes = Base64.decode(rawValue, Base64.DEFAULT);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}

