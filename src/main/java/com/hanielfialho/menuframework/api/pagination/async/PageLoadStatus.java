package com.hanielfialho.menuframework.api.pagination.async;

/** Current phase of an {@link AsyncPageState}. */
public enum PageLoadStatus {

  /** A request is in progress. */
  LOADING,

  /** A page was loaded and validated. */
  READY,

  /** Loading or an associated state transition failed. */
  ERROR
}
