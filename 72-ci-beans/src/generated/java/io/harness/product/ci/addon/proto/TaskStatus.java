// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: product/ci/addon/proto/progress.proto

package io.harness.product.ci.addon.proto;

/**
 * Protobuf enum {@code io.harness.product.ci.addon.proto.TaskStatus}
 */
@javax.annotation.Generated(value = "protoc", comments = "annotations:TaskStatus.java.pb.meta")
public enum TaskStatus implements com
.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>PENDING = 0;</code>
   */
  PENDING(0),
      /**
       * <code>RUNNING = 1;</code>
       */
      RUNNING(1),
      /**
       * <code>SUCCEEDED = 2;</code>
       */
      SUCCEEDED(2),
      /**
       * <code>FAILED = 3;</code>
       */
      FAILED(3), UNRECOGNIZED(-1),
      ;

  /**
   * <code>PENDING = 0;</code>
   */
  public static final int PENDING_VALUE = 0;
  /**
   * <code>RUNNING = 1;</code>
   */
  public static final int RUNNING_VALUE = 1;
  /**
   * <code>SUCCEEDED = 2;</code>
   */
  public static final int SUCCEEDED_VALUE = 2;
  /**
   * <code>FAILED = 3;</code>
   */
  public static final int FAILED_VALUE = 3;

  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException("Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static TaskStatus valueOf(int value) {
    return forNumber(value);
  }

  /**
   * @param value The numeric wire value of the corresponding enum entry.
   * @return The enum associated with the given numeric wire value.
   */
  public static TaskStatus forNumber(int value) {
    switch (value) {
      case 0:
        return PENDING;
      case 1:
        return RUNNING;
      case 2:
        return SUCCEEDED;
      case 3:
        return FAILED;
      default:
        return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<TaskStatus> internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<TaskStatus> internalValueMap =
      new com.google.protobuf.Internal.EnumLiteMap<TaskStatus>() {
        public TaskStatus findValueByNumber(int number) {
          return TaskStatus.forNumber(number);
        }
      };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor getValueDescriptor() {
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor getDescriptor() {
    return io.harness.product.ci.addon.proto.Progress.getDescriptor().getEnumTypes().get(0);
  }

  private static final TaskStatus[] VALUES = values();

  public static TaskStatus valueOf(com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException("EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private TaskStatus(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:io.harness.product.ci.addon.proto.TaskStatus)
}
