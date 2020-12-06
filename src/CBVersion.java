/*
 * MIT License
 * Copyright (c) 2020 Curt Binder
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

public class CBVersion implements Comparable {
    private String version;
    private int major;
    private int minor;
    private int build;
    private static final String DELIMITER = "[\\.-]";

    public CBVersion(String v) {
        version = v;
        major = 0;
        minor = 0;
        build = 0;
        String[] parts = v.split(DELIMITER);
        if (parts.length >= 3) {
            major = Integer.parseInt(parts[0]);
            minor = Integer.parseInt(parts[1]);
            build = Integer.parseInt(parts[2]);
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getBuild() {
        return build;
    }

    public String toString() {
        return "Major: " + major + ", Minor: " + minor + ", Build: " + build;
    }

    @Override
    public boolean equals(Object o) {
        // self check
        if (this == o) {
            return true;
        }
        // null check
        if (!(o instanceof CBVersion)) {
            return false;
        }

        CBVersion v = (CBVersion) o;
        // field comparison
        return Integer.compare(major, v.getMajor()) == 0 &&
            Integer.compare(minor, v.getMinor()) == 0 &&
            Integer.compare(build, v.getBuild()) == 0;
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (!(o instanceof CBVersion)) {
            throw new ClassCastException();
        }
        CBVersion v = (CBVersion) o;
        int result = Integer.compare(major, v.getMajor());
        if (result != 0) {
            return result;
        }
        result = Integer.compare(minor, v.getMinor());
        if (result != 0) {
            return result;
        }
        result = Integer.compare(build, v.getBuild());
        if (result != 0) {
            return result;
        }
        return 0;
    }
}
