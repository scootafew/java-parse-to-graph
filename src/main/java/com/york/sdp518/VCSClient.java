package com.york.sdp518;

import java.net.URI;

public interface VCSClient {

    URI clone(String uri) throws VCSClientException;

}
