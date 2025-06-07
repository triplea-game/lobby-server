package org.triplea.dropwizard.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IpAddressExtractorTest {

  @Mock private HttpServletRequest httpServletRequest;

  @ParameterizedTest
  @ValueSource(
      strings = {
        "127.0.0.1",
        "3ffe:1900:4545:3:200:f8ff:fe21:67cf",
        "[3ffe:1900:4545:3:200:f8ff:fe21:67cf]",
        "2600:4041:5d8e:d900:a19e:b761:e274:7139",
      })
  void extract(String ip) {
    when(httpServletRequest.getRemoteAddr()).thenReturn(ip);
    assertThat(IpAddressExtractor.extractIpAddress(httpServletRequest), is(ip));
  }
}
