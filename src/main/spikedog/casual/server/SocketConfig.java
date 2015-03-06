package spikedog.casual.server;

import java.net.Socket;

/**
 * Basic config for {@link CasualServer} sockets. Configures the underlying {@link Socket}.
 */
public final class SocketConfig {
  public static final class Builder {
    private Boolean keepAlive;
    private Integer socketTimeout;
    private Integer receiveBufferSize;
    private Integer sendBufferSize;
    private Boolean tcpNoDelay;

    public Builder setKeepAlive(Boolean keepAlive) {
      this.keepAlive = keepAlive;
      return this;
    }

    public Builder setSocketTimeout(Integer socketTimeout) {
      this.socketTimeout = socketTimeout;
      return this;
    }

    public Builder setReceiveBufferSize(Integer receiveBufferSize) {
      this.receiveBufferSize = receiveBufferSize;
      return this;
    }

    public Builder setSendBufferSize(Integer sendBufferSize) {
      this.sendBufferSize = sendBufferSize;
      return this;
    }

    public Builder setTcpNoDelay(Boolean tcpNoDelay) {
      this.tcpNoDelay = tcpNoDelay;
      return this;
    }

    public SocketConfig build() {
      return new SocketConfig(keepAlive, socketTimeout, receiveBufferSize, sendBufferSize,
          tcpNoDelay);
    }
  }

  private final Boolean keepAlive;
  private final Integer socketTimeout;
  private final Integer receiveBufferSize;
  private final Integer sendBufferSize;
  private final Boolean tcpNoDelay;

  private SocketConfig(Boolean keepAlive, Integer socketTimeout, Integer receiveBufferSize,
      Integer sendBufferSize, Boolean tcpNoDelay) {
    this.keepAlive = keepAlive;
    this.socketTimeout = socketTimeout;
    this.receiveBufferSize = receiveBufferSize;
    this.sendBufferSize = sendBufferSize;
    this.tcpNoDelay = tcpNoDelay;
  }

  public Boolean getKeepAlive() {
    return keepAlive;
  }

  public Integer getSocketTimeout() {
    return socketTimeout;
  }

  public Integer getReceiveBufferSize() {
    return receiveBufferSize;
  }

  public Integer getSendBufferSize() {
    return sendBufferSize;
  }

  public Boolean getTcpNoDelay() {
    return tcpNoDelay;
  }
}
