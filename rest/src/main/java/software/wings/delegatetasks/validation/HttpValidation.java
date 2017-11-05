package software.wings.delegatetasks.validation;

import static com.google.common.base.Ascii.toUpperCase;
import static java.util.Collections.singletonList;

import com.google.common.base.Splitter;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import software.wings.beans.DelegateTask;
import software.wings.delegatetasks.validation.DelegateConnectionResult.DelegateConnectionResultBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by brett on 11/2/17
 */
public class HttpValidation extends AbstractDelegateValidateTask {
  private static final Splitter HEADERS_SPLITTER = Splitter.on(",").trimResults().omitEmptyStrings();
  private static final Splitter HEADER_SPLITTER = Splitter.on(":").trimResults();

  public HttpValidation(
      String delegateId, DelegateTask delegateTask, Consumer<List<DelegateConnectionResult>> postExecute) {
    super(delegateId, delegateTask, postExecute);
  }

  @Override
  public List<DelegateConnectionResult> validate() {
    Object[] parameters = getParameters();
    return validate((String) parameters[0], (String) parameters[1], (String) parameters[2], (String) parameters[3],
        (Integer) parameters[4]);
  }

  private List<DelegateConnectionResult> validate(
      String method, String url, String body, String headers, int socketTimeoutMillis) {
    DelegateConnectionResultBuilder resultBuilder = DelegateConnectionResult.builder();

    SSLContextBuilder builder = new SSLContextBuilder();
    try {
      builder.loadTrustMaterial((x509Certificates, s) -> true);
    } catch (NoSuchAlgorithmException | KeyStoreException e) {
      e.printStackTrace();
    }
    SSLConnectionSocketFactory sslsf = null;
    try {
      sslsf = new SSLConnectionSocketFactory(builder.build(), (s, sslSession) -> true);
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      e.printStackTrace();
    }

    RequestConfig.Builder requestBuilder = RequestConfig.custom();
    requestBuilder = requestBuilder.setConnectTimeout(2000);
    requestBuilder = requestBuilder.setSocketTimeout(socketTimeoutMillis);

    CloseableHttpClient httpclient =
        HttpClients.custom().setSSLSocketFactory(sslsf).setDefaultRequestConfig(requestBuilder.build()).build();

    HttpUriRequest httpUriRequest = getHttpUriRequest(method, url, body, headers);

    resultBuilder.criteria(httpUriRequest.getURI().toString());
    try {
      HttpResponse httpResponse = httpclient.execute(httpUriRequest);
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if ((responseCode >= 200 && responseCode <= 399) || responseCode == 401 || responseCode == 403) {
        resultBuilder.validated(true);
      } else {
        resultBuilder.validated(false);
      }
    } catch (IOException e) {
      resultBuilder.validated(false);
    }

    return singletonList(resultBuilder.build());
  }

  private HttpUriRequest getHttpUriRequest(String method, String url, String body, String headers) {
    HttpUriRequest httpUriRequest;

    switch (toUpperCase(method)) {
      case "GET":
        httpUriRequest = new HttpGet(url);
        break;
      case "POST":
        HttpPost post = new HttpPost(url);
        if (body != null) {
          post.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        }
        httpUriRequest = post;
        break;
      case "PUT":
        HttpPut put = new HttpPut(url);
        if (body != null) {
          put.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        }
        httpUriRequest = put;
        break;
      case "DELETE":
        httpUriRequest = new HttpDelete(url);
        break;
      case "HEAD":
      default:
        httpUriRequest = new HttpHead(url);
    }

    if (headers != null) {
      for (String header : HEADERS_SPLITTER.split(headers)) {
        List<String> headerPair = HEADER_SPLITTER.splitToList(header);

        if (headerPair.size() == 2) {
          httpUriRequest.addHeader(headerPair.get(0), headerPair.get(1));
        }
      }
    }
    return httpUriRequest;
  }

  @Override
  public List<String> getCriteria() {
    Object[] parameters = getParameters();
    return singletonList(getHttpUriRequest(
        (String) parameters[0], (String) parameters[1], (String) parameters[2], (String) parameters[3])
                             .getURI()
                             .toString());
  }
}
