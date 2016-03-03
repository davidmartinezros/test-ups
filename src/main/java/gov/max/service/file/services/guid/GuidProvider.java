package gov.max.service.file.services.guid;

/**
 * GUID provider.
 */
public interface GuidProvider {

    /**
     * Returns generated GUID.
     *
     * @return the GUID.
     * @throws GuidException
     */
    public String getGuid() throws GuidException;

}