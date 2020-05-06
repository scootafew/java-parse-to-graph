package com.york.sdp518.service;

import com.york.sdp518.exception.VCSClientException;

import java.io.File;

public interface VCSClient {

    File clone(String uri) throws VCSClientException;

}
