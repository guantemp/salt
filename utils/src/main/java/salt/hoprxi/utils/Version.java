/*
 * Copyright (c) 2022. www.hoprxi.com All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package salt.hoprxi.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="www.hoprxi.com/authors/guan xiangHuan">guan xiangHuan</a>
 * @version 0.2.0 2019-02-22
 * @since JDK8.0
 */
public final class Version implements Comparable<Version> {
    public static final Version UNKNOWN = new Version(0, 0, 0, LocalDate.of(1970, 1, 1));
    private static final Pattern PATTERN = Pattern
            .compile("(^\\d*.\\d*.\\d*) ?[B|b]uilder ?(([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})(((0[13578]|1[02])(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8]))))");
    // The major number (ie 1.x.x).
    private int major;
    //The micro version number (ie x.x.1).
    private int revision;
    // The minor version number (ie x.1.x).
    private int minor;
    //build date (ie 20110106)
    private LocalDate builder;

    /**
     * @param major
     * @param minor
     * @param revision
     * @param builder
     */
    public Version(int major, int minor, int revision, LocalDate builder) {
        setMajor(major);
        setMinor(minor);
        setRevision(revision);
        this.builder = Objects.requireNonNull(builder, "builder is required");
    }

    /**
     * @param version such as 1.1.12 Builder 20171104
     * @return unknown if not petty format
     */
    public static Version of(String version) {
        Objects.requireNonNull(version, "version is required");
        Matcher matcher = PATTERN.matcher(version);
        if (matcher.find()) {
            String[] tmp = matcher.group(1).split("\\.");
            int major = NumberHelper.intOf(tmp[0]);
            int minor = NumberHelper.intOf(tmp[1]);
            int revision = NumberHelper.intOf(tmp[2]);
            LocalDate builder = LocalDate.parse(matcher.group(2), DateTimeFormatter.BASIC_ISO_DATE);
            return new Version(major, minor, revision, builder);
        }
        return UNKNOWN;
    }

    public static void main(String[] args) {
        System.out.println("1: " + Version.of("3.2.0 Builder 20181104"));
        System.out.println("2: " + Version.of("2.45.64 Builder 20171104"));
        System.out.println("3: " + Version.of("0.0.32 Builder 20170504"));
    }

    private void setRevision(int revision) {
        if (revision < 0)
            throw new IllegalArgumentException("The revision must large zero");
        this.revision = revision;
    }

    private void setMinor(int minor) {
        if (major < 0)
            throw new IllegalArgumentException("The minor must large zero");
        this.minor = minor;
    }

    private void setMajor(int major) {
        if (major < 0)
            throw new IllegalArgumentException("Thr major must large zero");
        this.major = major;
    }

    @Override
    public int compareTo(Version o) {
        int temp = o.major;
        int result = major > temp ? 1 : major < temp ? -1 : 0;
        if (result == 0) {
            temp = o.minor;
            result = revision > temp ? 1 : revision < temp ? -1 : 0;
            if (result == 0) {
                temp = o.revision;
                result = minor > temp ? 1 : minor < temp ? -1 : 0;
                if (result == 0) {
                    result = this.builder.compareTo(o.builder);
                }
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;

        if (major != version.major) return false;
        if (revision != version.revision) return false;
        if (minor != version.minor) return false;
        return builder.equals(version.builder);
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + revision;
        result = 31 * result + minor;
        result = 31 * result + builder.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + revision +
                " Builder " + builder.format(DateTimeFormatter.BASIC_ISO_DATE);
    }
}
