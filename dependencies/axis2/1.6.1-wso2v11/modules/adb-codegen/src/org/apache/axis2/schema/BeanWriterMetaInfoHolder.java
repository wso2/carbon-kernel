/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.schema;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used as a holder to pass on the meta information to the bean writer.
 * This meta information is used by the writer to write the databinding conversion code.
 * Note - Metainfholders are not meant to be reused!!!. They are per-class basis and are strictly
 * not thread safe!!!!
 */
public class BeanWriterMetaInfoHolder {


    protected boolean ordered = false;
    protected boolean anonymous = false;
    protected boolean choice = false;
    protected boolean simple = false;

    protected boolean extension = false;
    protected boolean restriction = false;
    private String extensionClassName = "";
    private String restrictionClassName = "";
    private QName extensionBaseType = null;
    private QName restrictionBaseType = null;
    protected Map<QName,QName> elementToSchemaQNameMap = new LinkedHashMap<QName,QName>();
    protected Map<QName,String> elementToJavaClassMap = new LinkedHashMap<QName,String>();
    protected Map<QName,Integer> specialTypeFlagMap = new LinkedHashMap<QName,Integer>();
    protected Map<QName,Long> qNameMaxOccursCountMap = new LinkedHashMap<QName,Long>();
    protected Map<QName,Long> qNameMinOccursCountMap = new LinkedHashMap<QName,Long>();
    protected Map<Integer,QName> qNameOrderMap = new LinkedHashMap<Integer,QName>();
    protected QName ownQname = null;
    protected String ownClassName = null;

    protected long lengthFacet = -1;
    protected long maxLengthFacet = -1;
    protected long minLengthFacet = -1;
    protected ArrayList<String> enumFacet = new ArrayList<String>();
    protected String patternFacet = null;
    protected String maxExclusiveFacet = null;
    protected String minExclusiveFacet = null;
    protected String maxInclusiveFacet = null;
    protected String minInclusiveFacet = null;

    protected Map<QName,String> memberTypes = new HashMap<QName,String>();
    protected Map<String,String> xmlNameJavaNameMap = new HashMap<String,String>();
    // TODO: why do we need this (instead of using memberTypes.keySet()?
    protected List<QName> memberTypesKeys = new ArrayList<QName>();

    protected Map<QName,String> elementQNameToDefulatValueMap = new HashMap<QName,String>();

    protected QName itemTypeQName;
    protected String itemTypeClassName;
    protected boolean isUnion;
    protected boolean isList;

    protected boolean isParticleClass;
    // keep whether this class has a partical class type variable
    protected boolean hasParticleType;

    protected List<QName> nillableQNameList = new ArrayList<QName>();

    //the parent metainfo holder, useful in handling extensions and
    //restrictions
    protected BeanWriterMetaInfoHolder parent = null;

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
    }

    public boolean isSimple() {
        return simple;
    }

    public void setSimple(boolean simple) {
        this.simple = simple;
    }

    public String getOwnClassName() {
        return ownClassName;
    }

    public void setOwnClassName(String ownClassName) {
        this.ownClassName = ownClassName;
    }

    public QName getOwnQname() {
        return ownQname;
    }

    public void setOwnQname(QName ownQname) {
        this.ownQname = ownQname;
    }

    /**
     * Gets the parent
     */
    public BeanWriterMetaInfoHolder getParent() {
        return parent;
    }

    /**
     * Gets the anonymous status.
     *
     * @return Returns boolean.
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Sets the anonymous flag.
     *
     * @param anonymous
     */
    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    /**
     * Sets the extensions base class name. Valid only when the isExtension
     * returns true.
     *
     * @return Returns String.
     */
    public String getExtensionClassName() {
        return extensionClassName;
    }

    /**
     * Sets the extensions base class name. Valid only when the isExtension
     * returns true.
     *
     * @param extensionClassName
     */
    public void setExtensionClassName(String extensionClassName) {
        this.extensionClassName = extensionClassName;
    }

    /**
     * Gets the extension status.
     *
     * @return Returns boolean.
     */
    public boolean isExtension() {
        return extension;
    }


    /**
     * Sets the extension status.
     *
     * @param extension
     */
    public void setExtension(boolean extension) {
        this.extension = extension;
    }

    public String getRestrictionClassName() {
        return restrictionClassName;
    }

    /**
     * Sets the restriction base class name. Valid only when the isRestriction
     * returns true.
     *
     * @param restrictionClassName
     */
    public void setRestrictionClassName(String restrictionClassName) {
        this.restrictionClassName = restrictionClassName;
    }

    /**
     * Gets the restriction status.
     *
     * @return Returns boolean.
     */
    public boolean isRestriction() {
        return restriction;
    }

    /**
     * Sets the restriction status.
     *
     * @param restriction
     */
    public void setRestriction(boolean restriction) {
        this.restriction = restriction;
    }

    /**
     * Sets the extension basetype.
     *
     * @param extensionBaseType
     */
    public void setExtensionBaseType(QName extensionBaseType) {
        this.extensionBaseType = extensionBaseType;
    }

    /**
     * Checks if it is a extension base type.
     *
     * @param extensionBaseType
     */
    public boolean isExtensionBaseType(QName extensionBaseType) {
        return (this.extensionBaseType == extensionBaseType);
    }

    /**
     * Sets the restriction basetype.
     *
     * @param restrictionBaseType
     */
    public void setRestrictionBaseType(QName restrictionBaseType) {
        this.restrictionBaseType = restrictionBaseType;
    }

    /**
     * Checks if it is a restriction base type.
     *
     * @param restrictionBaseType
     */
    public boolean isRestrictionBaseType(QName restrictionBaseType) {
        QName baseTypeQName = this.elementToSchemaQNameMap.get(restrictionBaseType);
        return (this.restrictionBaseType != null) && (baseTypeQName != null) &&
                this.restrictionBaseType.equals(baseTypeQName);
    }

    /**
     * Gets the ordered status.
     *
     * @return Returns boolean.
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * Sets the ordered flag.
     *
     * @param ordered
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /**
     * Registers a mapping.
     *
     * @param qName
     * @param schemaName
     * @param javaClassName
     */
    public void registerMapping(QName qName, QName schemaName, String javaClassName) {
        registerMapping(qName, schemaName, javaClassName, SchemaConstants.ELEMENT_TYPE);
    }


    /**
     * Registers a Qname as nillable
     * The qName better be of an element
     *
     * @param qName
     * @param schemaName
     * @param javaClassName
     */
    public void registerNillableQName(QName eltQName) {
        nillableQNameList.add(eltQName);
    }

    /**
     * Returns whether a QName is nillable or not
     *
     * @param eltQName
     */
    public boolean isNillable(QName eltQName) {
        return nillableQNameList.contains(eltQName);
    }

    /**
     * Registers a mapping.
     *
     * @param qName
     * @param schemaName
     * @param javaClassName
     * @param type
     */
    public void registerMapping(QName qName, QName schemaName, String javaClassName, int type) {
        this.elementToJavaClassMap.put(qName, javaClassName);
        this.elementToSchemaQNameMap.put(qName, schemaName);
        addtStatus(qName, type);

    }

    /**
     * this method registers the defult value agaist the element qname.
     * @param qname
     * @param value
     */
    public void registerDefaultValue(QName qname,String value){
        this.elementQNameToDefulatValueMap.put(qname,value);
    }

    /**
     *
     * @param qname
     * @return is a default value available for this qname
     */
    public boolean isDefaultValueAvailable(QName qname){
        return this.elementQNameToDefulatValueMap.containsKey(qname);
    }

    /**
     * gets the default value for qname
     * @param qname
     * @return default value for this qname
     */

    public String getDefaultValueForQName(QName qname){
        return this.elementQNameToDefulatValueMap.get(qname);
    }

    /**
     * Gets the schema name for the given QName.
     *
     * @param eltQName
     * @return Returns QName.
     */
    public QName getSchemaQNameForQName(QName eltQName) {
        return this.elementToSchemaQNameMap.get(eltQName);
    }

    /**
     * Gets the class name for the QName.
     *
     * @param eltQName
     * @return Returns String.
     */
    public String getClassNameForQName(QName eltQName) {
        return this.elementToJavaClassMap.get(eltQName);
    }

    /**
     * Gets whether a given QName is an attribute
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getAttributeStatusForQName(QName qName) {

        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(), SchemaConstants.ATTRIBUTE_TYPE);
    }

    /**
     * checks the element corresponds to the qName type is xsd:anyType
     *
     * @param qName
     * @return is element corresponds to qName has xsd:anyType
     */

    public boolean getDefaultStatusForQName(QName qName) {
        boolean isDefault = false;
        QName schemaTypeQName = this.elementToSchemaQNameMap.get(qName);
        if (schemaTypeQName != null) {
            isDefault = schemaTypeQName.equals(SchemaConstants.XSD_ANYTYPE);
        }
        return isDefault;
    }


    /**
     * Gets whether a given QName represents a anyType
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getAnyStatusForQName(QName qName) {
        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(), SchemaConstants.ANY_TYPE);
    }

    /**
     * Gets whether a given QName refers to an array.
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getArrayStatusForQName(QName qName) {
        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(),
                SchemaConstants.ARRAY_TYPE);
    }

    /**
     * Gets whether a given QName refers to binary.
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getBinaryStatusForQName(QName qName) {
        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(),
                SchemaConstants.BINARY_TYPE);
    }

    /**
     *
     * @param qName
     * @return is this a inner choice
     */

    public boolean getInnerChoiceStatusForQName(QName qName){
        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(),
                SchemaConstants.INNER_CHOICE_ELEMENT);
    }

    /**
     * Gets whether a given QName refers to Simple Type.
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getSimpleStatusForQName(QName qName) {
        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(),
                SchemaConstants.SIMPLE_TYPE_OR_CONTENT);
    }

    /**
     *
     * @param qName
     * @return whether the attribute is a partical class or not
     */

    public boolean getParticleTypeStatusForQName(QName qName){
        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(),
                SchemaConstants.PARTICLE_TYPE_ELEMENT);
    }

    /**
     * Gets whether a given QName has the any attribute status.
     *
     * @param qName
     * @return Returns boolean.
     */
    public boolean getAnyAttributeStatusForQName(QName qName) {
        return getArrayStatusForQName(qName) &&
                getAnyStatusForQName(qName);
    }

    /**
     * Gets whether a given QName has the optional attribute status.
     *
     * @param qName QName of attribute
     * @return Returns <code>true</code> if attribute has optional status
     */
    public boolean getOptionalAttributeStatusForQName(QName qName) {
        Integer state = specialTypeFlagMap.get(qName);
        return state != null && getStatus(state.intValue(),
                SchemaConstants.OPTIONAL_TYPE);
    }

    /**
     * Clears the whole set of tables.
     */
    public void clearTables() {
        this.elementToJavaClassMap.clear();
        this.elementToSchemaQNameMap.clear();
        this.elementToSchemaQNameMap.clear();
        this.elementToJavaClassMap.clear();
        this.specialTypeFlagMap.clear();
        this.qNameMaxOccursCountMap.clear();
        this.qNameMinOccursCountMap.clear();
        this.qNameOrderMap.clear();
        this.elementQNameToDefulatValueMap.clear();
    }

    /**
     * Adds the minOccurs associated with a QName.
     *
     * @param qName
     * @param minOccurs
     */
    public void addMinOccurs(QName qName, long minOccurs) {
        this.qNameMinOccursCountMap.put(qName, minOccurs);
    }

    /**
     * Registers a QName for the order.
     *
     * @param qName
     * @param index
     */
    public void registerQNameIndex(QName qName, int index) {
        this.qNameOrderMap.put(index, qName);
    }

    /**
     * Adds the minOccurs associated with a QName.
     *
     * @param qName
     * @return Returns long.
     */
    public long getMinOccurs(QName qName) {
        Long l = this.qNameMinOccursCountMap.get(qName);
        return l != null ? l.longValue() : 1; //default for min is 1
    }

    /**
     * Gets the maxOccurs associated with a QName.
     *
     * @param qName
     * @return Returns long.
     */
    public long getMaxOccurs(QName qName) {
        Long l = this.qNameMaxOccursCountMap.get(qName);
        return l != null ? l.longValue() : 1; //default for max is 1
    }

    /**
     * Adds the maxOccurs associated with a QName.
     *
     * @param qName
     * @param maxOccurs
     */
    public void addMaxOccurs(QName qName, long maxOccurs) {
        this.qNameMaxOccursCountMap.put(qName, maxOccurs);
    }

    /**
     * @return Returns Iterator.
     * @deprecated Use #getQNameArray
     */
    public Iterator<QName> getElementQNameIterator() {
        return elementToJavaClassMap.keySet().iterator();
    }

    /**
     * Gets the QName array - may not be ordered.
     *
     * @return Returns QName[].
     */
    public QName[] getQNameArray() {
        Set<QName> keySet = elementToJavaClassMap.keySet();
        return keySet.toArray(new QName[keySet.size()]);
    }

    /**
     * Gets the ordered QName array - useful in sequences where the order needs to be preserved
     * Note - #registerQNameIndex needs to be called if this is to work properly!
     *
     * @return Returns QName[].
     */
    public QName[] getOrderedQNameArray() {
        //get the keys of the order map
        Set<Integer> set = qNameOrderMap.keySet();
        int count = set.size();
        Integer[] keys = set.toArray(new Integer[count]);
        Arrays.sort(keys);

        //Now refill the Ordered QName Array
        List<QName> returnQNames = new ArrayList<QName>();
        for (int i = 0; i < keys.length; i++) {
            returnQNames.add(qNameOrderMap.get(keys[i]));
        }

        //we've missed the attributes, so if there are attributes
        //add them explicitly to the end of this list
        QName[] allNames = getQNameArray();
        for (int i = 0; i < allNames.length; i++) {
            if (getAttributeStatusForQName(allNames[i])) {
                returnQNames.add(allNames[i]);
            }
        }

        return returnQNames.toArray(new QName[returnQNames.size()]);
    }

    /**
     * Finds the starting count for the addition of new items to the order
     *
     * @return the starting number for the sequence
     */
    public int getOrderStartPoint() {
        return qNameOrderMap.size();
    }


    /**
     * Creates link to th
     *
     * @param metaInfo
     */
    public void setAsParent(BeanWriterMetaInfoHolder metaInfo) {
        parent = metaInfo;
    }

    /**
     * Adds a another status to a particular Qname.
     * A Qname can be associated with multiple status flags
     * and they all will be preserved
     *
     * @param type
     * @param mask
     */

    public void addtStatus(QName type, int mask) {
        Integer preValue = this.specialTypeFlagMap.get(type);
        if (preValue != null) {
            this.specialTypeFlagMap.put(type, preValue | mask);
        } else {
            this.specialTypeFlagMap.put(type, mask);
        }

    }


    private boolean getStatus(int storedStatus, int mask) {
        //when the mask is anded with the status then we should get
        //the mask it self!
        return (mask == (mask & storedStatus));
    }

    /**
     * Sets the length facet.
     *
     * @param lengthFacet
     */
    public void setLengthFacet(long lengthFacet) {
        this.lengthFacet = lengthFacet;
    }

    /**
     * Gets the length facet.
     *
     * @return Returns length facet.
     */
    public long getLengthFacet() {
        return this.lengthFacet;
    }

    /**
     * Sets the maxExclusive.
     *
     * @param maxExclusiveFacet
     */
    public void setMaxExclusiveFacet(String maxExclusiveFacet) {
        this.maxExclusiveFacet = maxExclusiveFacet;
    }

    /**
     * Gets the maxExclusive.
     *
     * @return Returns the maxExclusive.
     */
    public String getMaxExclusiveFacet() {
        return this.maxExclusiveFacet;
    }

    /**
     * Sets the minExclusive.
     *
     * @param minExclusiveFacet
     */
    public void setMinExclusiveFacet(String minExclusiveFacet) {
        this.minExclusiveFacet = minExclusiveFacet;
    }

    /**
     * Gets the minExclusive.
     *
     * @return Returns the minExclusive.
     */
    public String getMinExclusiveFacet() {
        return this.minExclusiveFacet;
    }

    /**
     * Sets the maxInclusive.
     *
     * @param maxInclusiveFacet
     */
    public void setMaxInclusiveFacet(String maxInclusiveFacet) {
        this.maxInclusiveFacet = maxInclusiveFacet;
    }

    /**
     * Gets the maxInclusive.
     *
     * @return Returns the maxInclusive.
     */
    public String getMaxInclusiveFacet() {
        return this.maxInclusiveFacet;
    }

    /**
     * Sets the minInclusive.
     *
     * @param minInclusiveFacet
     */
    public void setMinInclusiveFacet(String minInclusiveFacet) {
        this.minInclusiveFacet = minInclusiveFacet;
    }

    /**
     * Gets the minInclusive.
     *
     * @return Returns the minInclusive.
     */
    public String getMinInclusiveFacet() {
        return this.minInclusiveFacet;
    }

    /**
     * Sets the maxLength.
     *
     * @param maxLengthFacet
     */
    public void setMaxLengthFacet(long maxLengthFacet) {
        this.maxLengthFacet = maxLengthFacet;
    }

    /**
     * Gets the maxLength.
     *
     * @return Returns maxLength.
     */
    public long getMaxLengthFacet() {
        return this.maxLengthFacet;
    }

    /**
     * Sets the minLength.
     *
     * @param minLengthFacet
     */
    public void setMinLengthFacet(long minLengthFacet) {
        this.minLengthFacet = minLengthFacet;
    }

    /**
     * Gets the minLength.
     *
     * @return Returns minLength.
     */
    public long getMinLengthFacet() {
        return this.minLengthFacet;
    }

    /**
     * Sets the enumeration.
     *
     * @param enumFacet
     */
    public void setEnumFacet(ArrayList<String> enumFacet) {
        this.enumFacet = enumFacet;
    }

    /**
     * Adds the enumeration.
     *
     * @param enumFacet
     */
    public void addEnumFacet(String enumFacet) {
        this.enumFacet.add(enumFacet);
    }

    /**
     * Gets the enumeration.
     *
     * @return Returns enumeration.
     */
    public List<String> getEnumFacet() {
        return this.enumFacet;
    }

    /**
     * Sets the pattern.
     *
     * @param patternFacet
     */
    public void setPatternFacet(String patternFacet) {
        this.patternFacet = patternFacet;
    }

    /**
     * Gets the pattern.
     *
     * @return Returns pattern.
     */
    public String getPatternFacet() {
        return this.patternFacet;
    }

    /**
     *
     * @return Returns is union
     */

    public boolean isUnion() {
        return isUnion;
    }

    public void setUnion(boolean union) {
        isUnion = union;
    }

    /**
     *
     * @return Returns memeber type in a union
     */

    public Map<QName,String> getMemberTypes() {
        return memberTypes;
    }

    public void setMemberTypes(Map<QName,String> memberTypes) {
        this.memberTypes = memberTypes;
    }

    public List<QName> getMemberTypesKeys() {
        return memberTypesKeys;
    }

    public void setMemberTypesKeys(List<QName> memberTypesKeys) {
        this.memberTypesKeys = memberTypesKeys;
    }

    public void addMemberType(QName qname,String className){
        this.memberTypes.put(qname,className);
        this.memberTypesKeys.add(qname);
    }

    public boolean isList() {
        return isList;
    }

    public void setList(boolean list) {
        isList = list;
    }

    public QName getItemTypeQName() {
        return itemTypeQName;
    }

    public void setItemTypeQName(QName itemTypeQName) {
        this.itemTypeQName = itemTypeQName;
    }

    public String getItemTypeClassName() {
        return itemTypeClassName;
    }

    public void setItemTypeClassName(String itemTypeClassName) {
        this.itemTypeClassName = itemTypeClassName;
    }

    public boolean isParticleClass() {
        return isParticleClass;
    }

    public void setParticleClass(boolean particleClass) {
        isParticleClass = particleClass;
    }

    public boolean isHasParticleType() {
        return hasParticleType;
    }

    public void setHasParticleType(boolean hasParticleType) {
        this.hasParticleType = hasParticleType;
    }

    public void addXmlNameJavaNameMapping(String xmlName,String javaName){
        this.xmlNameJavaNameMap.put(xmlName,javaName);
    }

    public boolean isJavaNameMappingAvailable(String xmlName){
        return this.xmlNameJavaNameMap.containsKey(xmlName);
    }

    public String getJavaName(String xmlName){
        return this.xmlNameJavaNameMap.get(xmlName);
    }

}
