package dev.petuska.container.util

import org.gradle.api.logging.Logger
import org.slf4j.Marker
import org.slf4j.MarkerFactory

/**
 * Standardised logging utilities with the common prefix
 */
public interface PrefixedLogger : Logger {
  public companion object {
    public operator fun invoke(prefix: String, logger: Logger): PrefixedLogger =
      object : PrefixedLogger, Logger by logger {
        override val marker: Marker = MarkerFactory.getMarker(prefix)
      }
  }

  /**
   * Log marker to use
   */
  public val marker: Marker

  /**
   * Logs at error level
   * @param message provider
   */
  public fun error(message: () -> String) {
    if (isErrorEnabled) {
      error(marker, "[${marker.name}] ${message()}")
    }
  }

  /**
   * Logs at warn level
   * @param message provider
   */
  public fun warn(message: () -> String) {
    if (isWarnEnabled) {
      warn(marker, "[${marker.name}] ${message()}")
    }
  }

  /**
   * Logs at info level
   * @param message provider
   */
  public fun info(message: () -> String) {
    if (isInfoEnabled) {
      info(marker, "[${marker.name}] ${message()}")
    }
  }

  /**
   * Logs at debug level
   * @param message provider
   */
  public fun debug(message: () -> String) {
    if (isDebugEnabled) {
      debug(marker, "[${marker.name}] ${message()}")
    }
  }
}
