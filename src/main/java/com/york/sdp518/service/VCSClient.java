package com.york.sdp518.service;

import com.york.sdp518.VCSClientException;

import java.net.URI;

public interface VCSClient {

    URI clone(String uri) throws VCSClientException;

}
