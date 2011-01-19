package org.brlcad.geometryservice;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Classes that implement this interface enable the client to interact
 * with the BRL-CAD geometry service. The functions provided by the interface
 * are as follows:
 * <p/>
 * <ol>
 * <li> Verify that the geometry service is reachable.</li>
 * <li> Download versioned geometry from the BRL-CAD Geometry Service.</li>
 * <li> Store downloaded geometry in a local cache.</li>
 * <li> Check the cache for a copy of the desired geometry before downloading.</li>
 * <li> Provide a catalog of the local cache.</li>
 * <li> Clear the local cache.</li>
 * <li> Enable queries of the BRL-CAD geometry service.</li>
 * <li> Enable retrieval of geometry metadata from the geometry service.<li>
 * <li> Provide an estimate for the memory footprint of prepped geometry for a specified geometry.</li>
 * </ol>
 * <p/>
 * Implementation can be performed in two stages. The first comprises downloading and
 * caching the geometries. The second covers querying geometries and metadata.
 */
public interface GeometryServiceSPI {

    /**
     * Version number that implies that the most recent version is desired.
     */
    final static int HEAD = -1;

    /**
     * Tests the connection to the geometry service.
     *
     * @return true if the connection is operational, false otherwise.
     * @throws IllegalStateException if the location of the geometry service has
     *                               not been specified.
     */
    boolean ping();

    /**
     * Loads the requested geometry.
     * Determines if the specified geometry is available from the local cache, and
     * if so returns a File object that points to the local copy. Otherwise, this
     * method retrieves the geometry specified from the geometry service. The
     * geometry to retrieve is specified by a name and a version number.
     * The download geometry is stored in the local cache as a .g file and a File
     * to the downloaded geometry is created and returned.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param version      the revision number of the geometry or HEAD for the latest version
     * @return a File object that points to the local on-disk geometry file.
     * @throws IllegalStateException         if the location of the geometry service has
     *                                       not been specified.
     * @throws java.io.FileNotFoundException if the geometry cannot be found.
     * @throws java.io.IOException           if the geometry service cannot be reached.
     */
    File get(String geometryName, int version) throws IOException;

    /**
     * Loads the requested geometry.
     * <p>Determines if the specified geometry is available from the local cache, and
     * if so returns a File object that points to the local copy. Otherwise, this
     * method retrieves the geometry specified from the geometry service. The
     * geometry to retrieve is specified by a name and a version string.
     * The download geometry is stored in the local cache as a .g file and a File
     * to the downloaded geometry is created and returned.</p>
     *
     * @param geometryName the name of the geometry, such as T62
     * @param versionTag   the name of a tagged version.
     * @return a File object that points to the local on-disk geometry file.
     * @throws IllegalStateException         if the location of the geometry service has
     *                                       not been specified.
     * @throws java.io.FileNotFoundException if the geometry cannot be found.
     * @throws java.io.IOException           if the geometry service cannot be reached.
     */
    File get(String geometryName, String versionTag) throws IOException;


    /**
     * Determines if the requested geometry is in the local cache. Should
     * be called before downloading geometry from the geometry service.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param version      the revision number of the geometry or HEAD for the latest version
     * @return a File object that points to the local on-disk geometry file.
     * @throws IllegalStateException if the location of the local cache has
     *                               not been specified.
     */
    boolean isInCache(String geometryName, int version);


    /**
     * Determines if the requested geometry is in the local cache. Should
     * be called before downloading geometry from the geometry service.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param versionTag   the name of a tagged version
     * @return a File object that points to the local on-disk geometry file.
     * @throws IllegalStateException if the location of the local cache has
     *                               not been specified.
     */
    boolean isInCache(String geometryName, String versionTag);

    /**
     * Removes all items from the local geometry cache.
     *
     * @throws IllegalStateException if the location of the local cache has
     *                               not been specified.
     */
    void clearCache();

    /**
     * Provides a list of all of the entries in the local cache.
     *
     * @return a list of the geometries in the cache.
     * @throws IllegalStateException if the location of the local cache has
     *                               not been specified.
     */
    List<CacheEntry> getCacheEntries();

    /**
     * Retrieves the metadata associated with the specified geometry.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param version      the revision number of the geometry or HEAD for the latest version
     * @return the metadata as a collection of key-value pairs.
     * @throws IllegalStateException         if the location of the geometry service has
     *                                       not been specified.
     * @throws java.io.FileNotFoundException if the geometry cannot be found.
     * @throws java.io.IOException           if the geometry service cannot be reached.
     */
    Map<String, String> getMetadata(String geometryName, int version) throws IOException;

    /**
     * Retrieves the metadata associated with the specified geometry.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param versionTag     the name of a tagged version
     * @return the metadata as a collection of key-value pairs.
     * @throws IllegalStateException         if the location of the geometry service has
     *                                       not been specified.
     * @throws java.io.FileNotFoundException if the geometry cannot be found.
     * @throws java.io.IOException           if the geometry service cannot be reached.
     */
    Map<String, String> getMetadata(String geometryName, String versionTag) throws IOException;

    /**
     * Executes a query of the geometry service.
     * @param query an object that defines the query. The exact form of the query is undetermined
     * at this time.
     * @return A list of CatalogEntries that d
     * @throws IllegalStateException         if the location of the geometry service has
     *                                       not been specified.
     * @throws java.io.IOException           if the geometry service cannot be reached.
     */
    List<CatalogEntry> query(Object query) throws IOException;

    /**
     * Provides an estimate of the memory usage of the prepped geometry
     * corresponding to the given geometry name and version.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param version      the revision number of the geometry or HEAD for the latest version
     * @throws IllegalStateException         if the location of the geometry service has
     *                                       not been specified.
     * @throws java.io.FileNotFoundException if the geometry cannot be found.
     * @throws java.io.IOException           if the geometry service cannot be reached.
     * @return the estimated memory usage.
     */
    long estimateFootprint(String geometryName, int version) throws IOException;

    /**
     * Provides an estimate of the memory usage of the prepped geometry
     * corresponding to the given geometry name and version.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param versionTag      the name of a tagged version
     * @throws IllegalStateException         if the location of the geometry service has
     *                                       not been specified.
     * @throws java.io.FileNotFoundException if the geometry cannot be found.
     * @throws java.io.IOException           if the geometry service cannot be reached.
     * @return the estimated memory usage.
     */
    long estimateFootprint(String geometryName, String versionTag) throws IOException;

    public interface CacheEntry {
        void setName(String name);

        String getName();

        void setVersion(String version);

        String getVersion();
    }

    public interface CatalogEntry extends CacheEntry {
        void setMetadata(Map<String, String> md);

        Map<String, String> getMetadata();
    }

}
