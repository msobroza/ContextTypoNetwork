/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package tools.interface_cuda;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import javax.annotation.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"cast", "rawtypes", "serial", "unchecked"})
@Generated(value = "Autogenerated by Thrift Compiler (0.9.3)", date = "2016-07-22")
public class DecodingInputWordNetwork implements org.apache.thrift.TBase<DecodingInputWordNetwork, DecodingInputWordNetwork._Fields>, java.io.Serializable, Cloneable, Comparable<DecodingInputWordNetwork> {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("DecodingInputWordNetwork");

  private static final org.apache.thrift.protocol.TField WORD_FIELD_DESC = new org.apache.thrift.protocol.TField("word", org.apache.thrift.protocol.TType.STRING, (short)1);
  private static final org.apache.thrift.protocol.TField ID_SEQUENCE_NETWORK_FIELD_DESC = new org.apache.thrift.protocol.TField("idSequenceNetwork", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField ID_CLIQUE_NETWORK_FIELD_DESC = new org.apache.thrift.protocol.TField("idCliqueNetwork", org.apache.thrift.protocol.TType.I32, (short)3);
  private static final org.apache.thrift.protocol.TField DIRECTION_FIELD_DESC = new org.apache.thrift.protocol.TField("direction", org.apache.thrift.protocol.TType.I32, (short)4);
  private static final org.apache.thrift.protocol.TField DISTANCE_FIELD_DESC = new org.apache.thrift.protocol.TField("distance", org.apache.thrift.protocol.TType.I32, (short)5);
  private static final org.apache.thrift.protocol.TField ID_SPLIT_FIELD_DESC = new org.apache.thrift.protocol.TField("idSplit", org.apache.thrift.protocol.TType.I32, (short)6);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new DecodingInputWordNetworkStandardSchemeFactory());
    schemes.put(TupleScheme.class, new DecodingInputWordNetworkTupleSchemeFactory());
  }

  public String word; // required
  public int idSequenceNetwork; // required
  public int idCliqueNetwork; // required
  public int direction; // required
  public int distance; // required
  public int idSplit; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    WORD((short)1, "word"),
    ID_SEQUENCE_NETWORK((short)2, "idSequenceNetwork"),
    ID_CLIQUE_NETWORK((short)3, "idCliqueNetwork"),
    DIRECTION((short)4, "direction"),
    DISTANCE((short)5, "distance"),
    ID_SPLIT((short)6, "idSplit");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // WORD
          return WORD;
        case 2: // ID_SEQUENCE_NETWORK
          return ID_SEQUENCE_NETWORK;
        case 3: // ID_CLIQUE_NETWORK
          return ID_CLIQUE_NETWORK;
        case 4: // DIRECTION
          return DIRECTION;
        case 5: // DISTANCE
          return DISTANCE;
        case 6: // ID_SPLIT
          return ID_SPLIT;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __IDSEQUENCENETWORK_ISSET_ID = 0;
  private static final int __IDCLIQUENETWORK_ISSET_ID = 1;
  private static final int __DIRECTION_ISSET_ID = 2;
  private static final int __DISTANCE_ISSET_ID = 3;
  private static final int __IDSPLIT_ISSET_ID = 4;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.WORD, new org.apache.thrift.meta_data.FieldMetaData("word", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.ID_SEQUENCE_NETWORK, new org.apache.thrift.meta_data.FieldMetaData("idSequenceNetwork", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.ID_CLIQUE_NETWORK, new org.apache.thrift.meta_data.FieldMetaData("idCliqueNetwork", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.DIRECTION, new org.apache.thrift.meta_data.FieldMetaData("direction", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.DISTANCE, new org.apache.thrift.meta_data.FieldMetaData("distance", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.ID_SPLIT, new org.apache.thrift.meta_data.FieldMetaData("idSplit", org.apache.thrift.TFieldRequirementType.DEFAULT, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(DecodingInputWordNetwork.class, metaDataMap);
  }

  public DecodingInputWordNetwork() {
  }

  public DecodingInputWordNetwork(
    String word,
    int idSequenceNetwork,
    int idCliqueNetwork,
    int direction,
    int distance,
    int idSplit)
  {
    this();
    this.word = word;
    this.idSequenceNetwork = idSequenceNetwork;
    setIdSequenceNetworkIsSet(true);
    this.idCliqueNetwork = idCliqueNetwork;
    setIdCliqueNetworkIsSet(true);
    this.direction = direction;
    setDirectionIsSet(true);
    this.distance = distance;
    setDistanceIsSet(true);
    this.idSplit = idSplit;
    setIdSplitIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public DecodingInputWordNetwork(DecodingInputWordNetwork other) {
    __isset_bitfield = other.__isset_bitfield;
    if (other.isSetWord()) {
      this.word = other.word;
    }
    this.idSequenceNetwork = other.idSequenceNetwork;
    this.idCliqueNetwork = other.idCliqueNetwork;
    this.direction = other.direction;
    this.distance = other.distance;
    this.idSplit = other.idSplit;
  }

  public DecodingInputWordNetwork deepCopy() {
    return new DecodingInputWordNetwork(this);
  }

  @Override
  public void clear() {
    this.word = null;
    setIdSequenceNetworkIsSet(false);
    this.idSequenceNetwork = 0;
    setIdCliqueNetworkIsSet(false);
    this.idCliqueNetwork = 0;
    setDirectionIsSet(false);
    this.direction = 0;
    setDistanceIsSet(false);
    this.distance = 0;
    setIdSplitIsSet(false);
    this.idSplit = 0;
  }

  public String getWord() {
    return this.word;
  }

  public DecodingInputWordNetwork setWord(String word) {
    this.word = word;
    return this;
  }

  public void unsetWord() {
    this.word = null;
  }

  /** Returns true if field word is set (has been assigned a value) and false otherwise */
  public boolean isSetWord() {
    return this.word != null;
  }

  public void setWordIsSet(boolean value) {
    if (!value) {
      this.word = null;
    }
  }

  public int getIdSequenceNetwork() {
    return this.idSequenceNetwork;
  }

  public DecodingInputWordNetwork setIdSequenceNetwork(int idSequenceNetwork) {
    this.idSequenceNetwork = idSequenceNetwork;
    setIdSequenceNetworkIsSet(true);
    return this;
  }

  public void unsetIdSequenceNetwork() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __IDSEQUENCENETWORK_ISSET_ID);
  }

  /** Returns true if field idSequenceNetwork is set (has been assigned a value) and false otherwise */
  public boolean isSetIdSequenceNetwork() {
    return EncodingUtils.testBit(__isset_bitfield, __IDSEQUENCENETWORK_ISSET_ID);
  }

  public void setIdSequenceNetworkIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __IDSEQUENCENETWORK_ISSET_ID, value);
  }

  public int getIdCliqueNetwork() {
    return this.idCliqueNetwork;
  }

  public DecodingInputWordNetwork setIdCliqueNetwork(int idCliqueNetwork) {
    this.idCliqueNetwork = idCliqueNetwork;
    setIdCliqueNetworkIsSet(true);
    return this;
  }

  public void unsetIdCliqueNetwork() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __IDCLIQUENETWORK_ISSET_ID);
  }

  /** Returns true if field idCliqueNetwork is set (has been assigned a value) and false otherwise */
  public boolean isSetIdCliqueNetwork() {
    return EncodingUtils.testBit(__isset_bitfield, __IDCLIQUENETWORK_ISSET_ID);
  }

  public void setIdCliqueNetworkIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __IDCLIQUENETWORK_ISSET_ID, value);
  }

  public int getDirection() {
    return this.direction;
  }

  public DecodingInputWordNetwork setDirection(int direction) {
    this.direction = direction;
    setDirectionIsSet(true);
    return this;
  }

  public void unsetDirection() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __DIRECTION_ISSET_ID);
  }

  /** Returns true if field direction is set (has been assigned a value) and false otherwise */
  public boolean isSetDirection() {
    return EncodingUtils.testBit(__isset_bitfield, __DIRECTION_ISSET_ID);
  }

  public void setDirectionIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __DIRECTION_ISSET_ID, value);
  }

  public int getDistance() {
    return this.distance;
  }

  public DecodingInputWordNetwork setDistance(int distance) {
    this.distance = distance;
    setDistanceIsSet(true);
    return this;
  }

  public void unsetDistance() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __DISTANCE_ISSET_ID);
  }

  /** Returns true if field distance is set (has been assigned a value) and false otherwise */
  public boolean isSetDistance() {
    return EncodingUtils.testBit(__isset_bitfield, __DISTANCE_ISSET_ID);
  }

  public void setDistanceIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __DISTANCE_ISSET_ID, value);
  }

  public int getIdSplit() {
    return this.idSplit;
  }

  public DecodingInputWordNetwork setIdSplit(int idSplit) {
    this.idSplit = idSplit;
    setIdSplitIsSet(true);
    return this;
  }

  public void unsetIdSplit() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __IDSPLIT_ISSET_ID);
  }

  /** Returns true if field idSplit is set (has been assigned a value) and false otherwise */
  public boolean isSetIdSplit() {
    return EncodingUtils.testBit(__isset_bitfield, __IDSPLIT_ISSET_ID);
  }

  public void setIdSplitIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __IDSPLIT_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case WORD:
      if (value == null) {
        unsetWord();
      } else {
        setWord((String)value);
      }
      break;

    case ID_SEQUENCE_NETWORK:
      if (value == null) {
        unsetIdSequenceNetwork();
      } else {
        setIdSequenceNetwork((Integer)value);
      }
      break;

    case ID_CLIQUE_NETWORK:
      if (value == null) {
        unsetIdCliqueNetwork();
      } else {
        setIdCliqueNetwork((Integer)value);
      }
      break;

    case DIRECTION:
      if (value == null) {
        unsetDirection();
      } else {
        setDirection((Integer)value);
      }
      break;

    case DISTANCE:
      if (value == null) {
        unsetDistance();
      } else {
        setDistance((Integer)value);
      }
      break;

    case ID_SPLIT:
      if (value == null) {
        unsetIdSplit();
      } else {
        setIdSplit((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case WORD:
      return getWord();

    case ID_SEQUENCE_NETWORK:
      return getIdSequenceNetwork();

    case ID_CLIQUE_NETWORK:
      return getIdCliqueNetwork();

    case DIRECTION:
      return getDirection();

    case DISTANCE:
      return getDistance();

    case ID_SPLIT:
      return getIdSplit();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case WORD:
      return isSetWord();
    case ID_SEQUENCE_NETWORK:
      return isSetIdSequenceNetwork();
    case ID_CLIQUE_NETWORK:
      return isSetIdCliqueNetwork();
    case DIRECTION:
      return isSetDirection();
    case DISTANCE:
      return isSetDistance();
    case ID_SPLIT:
      return isSetIdSplit();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof DecodingInputWordNetwork)
      return this.equals((DecodingInputWordNetwork)that);
    return false;
  }

  public boolean equals(DecodingInputWordNetwork that) {
    if (that == null)
      return false;

    boolean this_present_word = true && this.isSetWord();
    boolean that_present_word = true && that.isSetWord();
    if (this_present_word || that_present_word) {
      if (!(this_present_word && that_present_word))
        return false;
      if (!this.word.equals(that.word))
        return false;
    }

    boolean this_present_idSequenceNetwork = true;
    boolean that_present_idSequenceNetwork = true;
    if (this_present_idSequenceNetwork || that_present_idSequenceNetwork) {
      if (!(this_present_idSequenceNetwork && that_present_idSequenceNetwork))
        return false;
      if (this.idSequenceNetwork != that.idSequenceNetwork)
        return false;
    }

    boolean this_present_idCliqueNetwork = true;
    boolean that_present_idCliqueNetwork = true;
    if (this_present_idCliqueNetwork || that_present_idCliqueNetwork) {
      if (!(this_present_idCliqueNetwork && that_present_idCliqueNetwork))
        return false;
      if (this.idCliqueNetwork != that.idCliqueNetwork)
        return false;
    }

    boolean this_present_direction = true;
    boolean that_present_direction = true;
    if (this_present_direction || that_present_direction) {
      if (!(this_present_direction && that_present_direction))
        return false;
      if (this.direction != that.direction)
        return false;
    }

    boolean this_present_distance = true;
    boolean that_present_distance = true;
    if (this_present_distance || that_present_distance) {
      if (!(this_present_distance && that_present_distance))
        return false;
      if (this.distance != that.distance)
        return false;
    }

    boolean this_present_idSplit = true;
    boolean that_present_idSplit = true;
    if (this_present_idSplit || that_present_idSplit) {
      if (!(this_present_idSplit && that_present_idSplit))
        return false;
      if (this.idSplit != that.idSplit)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    List<Object> list = new ArrayList<Object>();

    boolean present_word = true && (isSetWord());
    list.add(present_word);
    if (present_word)
      list.add(word);

    boolean present_idSequenceNetwork = true;
    list.add(present_idSequenceNetwork);
    if (present_idSequenceNetwork)
      list.add(idSequenceNetwork);

    boolean present_idCliqueNetwork = true;
    list.add(present_idCliqueNetwork);
    if (present_idCliqueNetwork)
      list.add(idCliqueNetwork);

    boolean present_direction = true;
    list.add(present_direction);
    if (present_direction)
      list.add(direction);

    boolean present_distance = true;
    list.add(present_distance);
    if (present_distance)
      list.add(distance);

    boolean present_idSplit = true;
    list.add(present_idSplit);
    if (present_idSplit)
      list.add(idSplit);

    return list.hashCode();
  }

  @Override
  public int compareTo(DecodingInputWordNetwork other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;

    lastComparison = Boolean.valueOf(isSetWord()).compareTo(other.isSetWord());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetWord()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.word, other.word);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIdSequenceNetwork()).compareTo(other.isSetIdSequenceNetwork());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIdSequenceNetwork()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.idSequenceNetwork, other.idSequenceNetwork);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIdCliqueNetwork()).compareTo(other.isSetIdCliqueNetwork());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIdCliqueNetwork()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.idCliqueNetwork, other.idCliqueNetwork);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDirection()).compareTo(other.isSetDirection());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDirection()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.direction, other.direction);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDistance()).compareTo(other.isSetDistance());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDistance()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.distance, other.distance);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetIdSplit()).compareTo(other.isSetIdSplit());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetIdSplit()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.idSplit, other.idSplit);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DecodingInputWordNetwork(");
    boolean first = true;

    sb.append("word:");
    if (this.word == null) {
      sb.append("null");
    } else {
      sb.append(this.word);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("idSequenceNetwork:");
    sb.append(this.idSequenceNetwork);
    first = false;
    if (!first) sb.append(", ");
    sb.append("idCliqueNetwork:");
    sb.append(this.idCliqueNetwork);
    first = false;
    if (!first) sb.append(", ");
    sb.append("direction:");
    sb.append(this.direction);
    first = false;
    if (!first) sb.append(", ");
    sb.append("distance:");
    sb.append(this.distance);
    first = false;
    if (!first) sb.append(", ");
    sb.append("idSplit:");
    sb.append(this.idSplit);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class DecodingInputWordNetworkStandardSchemeFactory implements SchemeFactory {
    public DecodingInputWordNetworkStandardScheme getScheme() {
      return new DecodingInputWordNetworkStandardScheme();
    }
  }

  private static class DecodingInputWordNetworkStandardScheme extends StandardScheme<DecodingInputWordNetwork> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, DecodingInputWordNetwork struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // WORD
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.word = iprot.readString();
              struct.setWordIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // ID_SEQUENCE_NETWORK
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.idSequenceNetwork = iprot.readI32();
              struct.setIdSequenceNetworkIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // ID_CLIQUE_NETWORK
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.idCliqueNetwork = iprot.readI32();
              struct.setIdCliqueNetworkIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // DIRECTION
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.direction = iprot.readI32();
              struct.setDirectionIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 5: // DISTANCE
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.distance = iprot.readI32();
              struct.setDistanceIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 6: // ID_SPLIT
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.idSplit = iprot.readI32();
              struct.setIdSplitIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, DecodingInputWordNetwork struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      if (struct.word != null) {
        oprot.writeFieldBegin(WORD_FIELD_DESC);
        oprot.writeString(struct.word);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldBegin(ID_SEQUENCE_NETWORK_FIELD_DESC);
      oprot.writeI32(struct.idSequenceNetwork);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(ID_CLIQUE_NETWORK_FIELD_DESC);
      oprot.writeI32(struct.idCliqueNetwork);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(DIRECTION_FIELD_DESC);
      oprot.writeI32(struct.direction);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(DISTANCE_FIELD_DESC);
      oprot.writeI32(struct.distance);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(ID_SPLIT_FIELD_DESC);
      oprot.writeI32(struct.idSplit);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class DecodingInputWordNetworkTupleSchemeFactory implements SchemeFactory {
    public DecodingInputWordNetworkTupleScheme getScheme() {
      return new DecodingInputWordNetworkTupleScheme();
    }
  }

  private static class DecodingInputWordNetworkTupleScheme extends TupleScheme<DecodingInputWordNetwork> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, DecodingInputWordNetwork struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      BitSet optionals = new BitSet();
      if (struct.isSetWord()) {
        optionals.set(0);
      }
      if (struct.isSetIdSequenceNetwork()) {
        optionals.set(1);
      }
      if (struct.isSetIdCliqueNetwork()) {
        optionals.set(2);
      }
      if (struct.isSetDirection()) {
        optionals.set(3);
      }
      if (struct.isSetDistance()) {
        optionals.set(4);
      }
      if (struct.isSetIdSplit()) {
        optionals.set(5);
      }
      oprot.writeBitSet(optionals, 6);
      if (struct.isSetWord()) {
        oprot.writeString(struct.word);
      }
      if (struct.isSetIdSequenceNetwork()) {
        oprot.writeI32(struct.idSequenceNetwork);
      }
      if (struct.isSetIdCliqueNetwork()) {
        oprot.writeI32(struct.idCliqueNetwork);
      }
      if (struct.isSetDirection()) {
        oprot.writeI32(struct.direction);
      }
      if (struct.isSetDistance()) {
        oprot.writeI32(struct.distance);
      }
      if (struct.isSetIdSplit()) {
        oprot.writeI32(struct.idSplit);
      }
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, DecodingInputWordNetwork struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      BitSet incoming = iprot.readBitSet(6);
      if (incoming.get(0)) {
        struct.word = iprot.readString();
        struct.setWordIsSet(true);
      }
      if (incoming.get(1)) {
        struct.idSequenceNetwork = iprot.readI32();
        struct.setIdSequenceNetworkIsSet(true);
      }
      if (incoming.get(2)) {
        struct.idCliqueNetwork = iprot.readI32();
        struct.setIdCliqueNetworkIsSet(true);
      }
      if (incoming.get(3)) {
        struct.direction = iprot.readI32();
        struct.setDirectionIsSet(true);
      }
      if (incoming.get(4)) {
        struct.distance = iprot.readI32();
        struct.setDistanceIsSet(true);
      }
      if (incoming.get(5)) {
        struct.idSplit = iprot.readI32();
        struct.setIdSplitIsSet(true);
      }
    }
  }

}

