/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.utils;

import static java.util.Objects.hash;

import java.net.URI;
import java.util.EnumSet;

/**
 * Structure for presenting the results of a fixity check
 * (and any repair operations that may have occurred)
 */
public class FixityResult {

    public static enum FixityState {
        SUCCESS, REPAIRED, BAD_CHECKSUM, BAD_SIZE
    }

    /**
     * This is a little weird here, and is vestigal from when
     * this was a JAX-B model as well.
     *
     * The "state" of the fixity object is one of:
     * - SUCCESS: the fixity check was declared successful
     * - BAD_CHECKSUM and/or BAD_SIZE: either the checksum or the size of the
     *       bitstream didn't match the stored size
     * - REPAIRED and BAD_*: the checksum or size failed to match, but it
     *       was automatically recovered from a different copy
     */
    public EnumSet<FixityState> status = EnumSet.noneOf(FixityState.class);

    public long computedSize;

    public URI computedChecksum;

    private final LowLevelCacheEntry entry;

    public FixityResult() {
        entry = null;
    }

    public FixityResult(final LowLevelCacheEntry entry) {
        this.entry = entry;
    }

    public FixityResult(final long size, final URI checksum) {
        entry = null;
        computedSize = size;
        computedChecksum = checksum;
    }

    public FixityResult(final LowLevelCacheEntry entry, final long size,
                        final URI checksum) {
        this.entry = entry;
        computedSize = size;
        computedChecksum = checksum;
    }

    public String getStoreIdentifier() {
        return entry.getExternalIdentifier();
    }

    @Override
    public boolean equals(final Object obj) {

        boolean result = false;
        if (obj instanceof FixityResult) {
            final FixityResult that = (FixityResult) obj;
            result =
                computedSize == that.computedSize &&
                computedChecksum.equals(that.computedChecksum);
        }

        return result;
    }

    @Override
    public int hashCode() {
        return hash(computedSize, computedChecksum);
    }

    @Override
    public String toString() {
        return "Fixity: checksum: " + computedChecksum.toString() + " / " +
            Long.toString(computedSize);
    }

    public LowLevelCacheEntry getEntry() {
        return entry;
    }

    public boolean matches(final URI checksum) {
        return computedChecksum.equals(checksum);
    }

    public boolean matches(final long size) {
        return computedSize == size;
    }

    /**
     * Does the fixity entry match the given size and checksum?
     * @param size bitstream size in bytes
     * @param checksum checksum URI in the form urn:DIGEST:RESULT
     * @return true if both conditions matched
     */
    public boolean matches(final long size, final URI checksum) {
        return matches(size) && matches(checksum);
    }

    public boolean isSuccess() {
        return status.contains(FixityState.SUCCESS);
    }

    public void setRepaired() {
        status.add(FixityState.REPAIRED);
    }
}
