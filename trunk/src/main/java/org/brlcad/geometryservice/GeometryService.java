package org.brlcad.geometryservice;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Classes that implement this interface enable the client to interact
 * with the BRL-CAD geometry service. The functions provided by the interface
 * are as follows:
 * <p/>
 * <ol>
 * <li> Download versioned geometry from the BRL-CAD Geometry Service and
 * store the downloaded geometry in a local file.</li>
 * <li> Enable queries of the BRL-CAD geometry service.</li>
 * <li> Enable retrieval of geometry metadata from the geometry service.<li>
 * <li> Provide an estimate for the memory footprint of prepped geometry for a specified geometry.</li>
 * </ol>
 * <p/>
 * Implementation can be performed in two stages. The first comprises downloading
 * the geometries. The second covers querying geometries and metadata.
 */
public interface GeometryService {

    /**
     * Version number that implies that the most recent version is desired.
     */
    final static int HEAD = -1;


    /**
     * Loads the requested geometry.
     * <p>Determines if the specified geometry is available from the BRL-CAD
     * geometry service and if so downloads the geometry. The geometry to retrieve
     * is specified by a name and a version string. The download geometry is stored on
     * the local disk as a .g file and a File
     * to the downloaded geometry is created and returned.</p>
     *
     * @param geometryName the name of the geometry, such as T62
     * @param version      the version.
     * @return a File object that points to the local on-disk geometry file.
     * @throws GeometryServiceException if the BRL-CAD service cannot be reached or the
     *                                  specified geometry does not exist.
     */
    File get(String geometryName, String version) throws GeometryServiceException;

    /**
     * Retrieves the metadata associated with the specified geometry.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param version      the name of version
     * @return the metadata as a collection of key-value pairs.
     * @throws GeometryServiceException if the BRL-CAD service cannot be reached or the
     *                                  specified geometry does not exist.
     */
    Map<String, String> getMetadata(String geometryName, String version) throws GeometryServiceException;

    /**
     * Executes a query of the geometry service.
     *
     * @param query an object that defines the query. The exact form of the query is undetermined
     *              at this time.
     * @return A list of CatalogEntries that match the query
     * @throws GeometryServiceException if the BRL-CAD service cannot be reached or
     *                                  the query is badly-formed..
     */
    List<CatalogEntry> query(Object query) throws GeometryServiceException;

    /**
     * Provides an estimate of the memory usage of the prepped geometry
     * corresponding to the given geometry name and version.
     *
     * @param geometryName the name of the geometry, such as T62
     * @param version      the version
     * @return the estimated memory usage.
     * @throws GeometryServiceException if the BRL-CAD service cannot be reached or the
     *                                  specified geometry does not exist.
     */
    long estimateFootprint(String geometryName, String version) throws GeometryServiceException;


}
