<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text"/>

     <!-- cater for the multiple classes - wrappped mode -->
    <xsl:template match="/beans">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="caps_axis2_name">ADB_<xsl:value-of select="@caps-name"/></xsl:variable>
        #ifndef <xsl:value-of select="$caps_axis2_name"/>_H
        #define <xsl:value-of select="$caps_axis2_name"/>_H

        /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2/Java version: #axisVersion# #today#
        */

        #include &lt;stdio.h&gt;
        #include &lt;axiom.h&gt;
        #include &lt;axis2_util.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;

        #include "axis2_extension_mapper.h"

        #ifdef __cplusplus
        extern "C"
        {
        #endif

        #define ADB_DEFAULT_DIGIT_LIMIT 1024
        #define ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT 64
        <xsl:if test="itemtype">
        #define ADB_DEFAULT_LIST_SEPERATOR " "
        </xsl:if>

        /**
        *  <xsl:value-of select="$axis2_name"/> wrapped class classes ( structure for C )
        */

        <xsl:apply-templates/>


        #ifdef __cplusplus
        }
        #endif

        #endif /* <xsl:value-of select="$caps_axis2_name"/>_H */
    </xsl:template>

    <!--cater for the multiple classes - unwrappped mode -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>


    <xsl:template match="class">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="caps_axis2_name">ADB_<xsl:value-of select="@caps-name"/></xsl:variable>

        <!-- checking for is union -->
        <xsl:variable name="isUnion" select="@union"/>

        #ifndef <xsl:value-of select="$caps_axis2_name"/>_H
        #define <xsl:value-of select="$caps_axis2_name"/>_H

       /**
        * <xsl:value-of select="$axis2_name"/>.h
        *
        * This file was auto-generated from WSDL
        * by the Apache Axis2/Java version: #axisVersion# #today#
        */

       /**
        *  <xsl:value-of select="$axis2_name"/> class
        */

        <xsl:for-each select="property">
          <xsl:if test="@ours">
          <xsl:variable name="propertyType" select="substring-before(@type, '_t*')"/>
          #include "<xsl:value-of select="$propertyType"/>.h"
          </xsl:if>
        </xsl:for-each>
        <!--include special headers-->
        <xsl:for-each select="property[@type='axutil_date_time_t*']">
          <xsl:if test="position()=1">
            #include &lt;axutil_date_time.h&gt;
          </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="property[@type='axutil_base64_binary_t*']">
          <xsl:if test="position()=1">
            #include &lt;axutil_base64_binary.h&gt;
          </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="property[@type='axutil_duration_t*']">
          <xsl:if test="position()=1">
            #include &lt;axutil_duration.h&gt;
          </xsl:if>
        </xsl:for-each>
        <xsl:for-each select="memberType">
          <xsl:if test="@ours">
          <xsl:variable name="propertyType" select="substring-before(@type, '_t*')"/>
          #include "<xsl:value-of select="$propertyType"/>.h"
          </xsl:if>
        </xsl:for-each>

        #include &lt;stdio.h&gt;
        #include &lt;axiom.h&gt;
        #include &lt;axis2_util.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;

        #include "axis2_extension_mapper.h"

        #ifdef __cplusplus
        extern "C"
        {
        #endif

        #define ADB_DEFAULT_DIGIT_LIMIT 1024
        #define ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT 64
        <xsl:if test="itemtype">
        #define ADB_DEFAULT_LIST_SEPERATOR " "
        </xsl:if>

        typedef struct <xsl:value-of select="$axis2_name"/><xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t;

        <!-- Check if this type is a supported enum -->
        <xsl:variable name="isEnum">
          <xsl:choose>
            <xsl:when test="count(property)=1 and property/enumFacet and property/@type='axis2_char_t*'">1</xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="$isEnum=1">
          <xsl:variable name="enum">adb_<xsl:value-of select="property/@name"/>_enum_t</xsl:variable>
          <xsl:variable name="propertyCapsCName" select="property/@caps-cname"/>
        /* Enumeration for this type */
        typedef enum {
            <xsl:for-each select="property/enumFacet">
                <xsl:text/><xsl:value-of select="$propertyCapsCName"/>_<xsl:value-of select="@id"/>
                <xsl:if test="position()!=last()">,
            </xsl:if>
          </xsl:for-each> } <xsl:value-of select="$enum"/>;
        </xsl:if>

        /******************************* Create and Free functions *********************************/

        /**
         * Constructor for creating <xsl:value-of select="$axis2_name"/>_t
         * @param env pointer to environment struct
         * @return newly created <xsl:value-of select="$axis2_name"/>_t object
         */
        <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create(
            const axutil_env_t *env );

        /**
         * Wrapper for the "free" function, will invoke the extension mapper instead
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object to free
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free (
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * Free <xsl:value-of select="$axis2_name"/>_t object
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object to free
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free_obj (
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);



        /********************************** Getters and Setters **************************************/
        <xsl:if test="count(property[@array])!=0">/******** Deprecated for array types, Use 'Getters and Setters for Arrays' instead ***********/</xsl:if>
        <xsl:if test="@choice">/******** In a case of a choose among elements, the last one to set will be chooosen *********/</xsl:if>
        <xsl:if test="@list">/******* This is a list, please use Getters and 'Setters for Array' Instead of following *****/</xsl:if>

        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
            <xsl:choose>
                <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment">
                <xsl:choose>
                    <xsl:when test="@isarray"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$nativePropertyType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="constValue">
                <xsl:choose>
                   <xsl:when test="@isarray"></xsl:when>
                   <xsl:when test="@type='axis2_char_t*' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'">const </xsl:when>
                </xsl:choose>
            </xsl:variable>
        

        /**
         * Getter for <xsl:value-of select="$propertyName"/>. <xsl:if test="@isarray">Deprecated for array types, Use <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at instead</xsl:if>
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return <xsl:value-of select="$paramComment"/>
         */
        <xsl:value-of select="$propertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * Setter for <xsl:value-of select="$propertyName"/>.<xsl:if test="@isarray">Deprecated for array types, Use <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_at
         * or <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/> instead.</xsl:if>
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param arg_<xsl:value-of select="$CName"/><xsl:text> </xsl:text> <xsl:value-of select="$paramComment"/>
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            <xsl:value-of select="$constValue"/><xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> arg_<xsl:value-of select="$CName"/>);

        /**
         * Resetter for <xsl:value-of select="$propertyName"/>
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        </xsl:for-each>

        <!-- The following take care of list items -->

        <xsl:for-each select="itemtype">
            <xsl:variable name="propertyType">axutil_array_list_t*</xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="$name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="$name"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:variable>
            <xsl:variable name="constValue">
                <xsl:choose>
                   <xsl:when test="@type='axis2_char_t*' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'">const </xsl:when>
                </xsl:choose>
            </xsl:variable>

        /**
         * Getter for <xsl:value-of select="$propertyName"/>. Deprecated for array types, Use <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at instead
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return <xsl:value-of select="$paramComment"/>
         */
        <xsl:value-of select="$propertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * Setter for <xsl:value-of select="$propertyName"/>. Deprecated for array types, Use <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_at
         * or <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/> instead.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param arg_<xsl:value-of select="$CName"/><xsl:text> </xsl:text> <xsl:value-of select="$paramComment"/>
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> arg_<xsl:value-of select="$CName"/>);

        /**
         * Resetter for <xsl:value-of select="$propertyName"/>
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        </xsl:for-each>


        <xsl:if test="count(property[@array])!=0 or count(itemtype)!=0">
        /****************************** Getters and Setters For Arrays **********************************/
        /************ Array Specific Operations: get_at, set_at, add, remove_at, sizeof *****************/

        /**
         * E.g. use of get_at, set_at, add and sizeof
         *
         * for(i = 0; i &lt; adb_element_sizeof_property(adb_object, env); i ++ )
         * {
         *     // Getting ith value to property_object variable
         *     property_object = adb_element_get_property_at(adb_object, env, i);
         *
         *     // Setting ith value from property_object variable
         *     adb_element_set_property_at(adb_object, env, i, property_object);
         *
         *     // Appending the value to the end of the array from property_object variable
         *     adb_element_add_property(adb_object, env, property_object);
         *
         *     // Removing the ith value from an array
         *     adb_element_remove_property_at(adb_object, env, i);
         *     
         * }
         *
         */

        </xsl:if>

        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
            <xsl:choose>
                <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment">
                <xsl:choose>
                    <xsl:when test="@isarray"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$nativePropertyType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="constValue">
                <xsl:choose>
                   <xsl:when test="@type='axis2_char_t*' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'">const </xsl:when>
                </xsl:choose>
            </xsl:variable>


        <xsl:if test="@isarray">
        
        /**
         * Get the ith element of <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param i index of the item to return
         * @return ith <xsl:value-of select="$nativePropertyType"/> of the array
         */
        <xsl:value-of select="$nativePropertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i);

        /**
         * Set the ith element of <xsl:value-of select="$propertyName"/>. (If the ith already exist, it will be replaced)
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param i index of the item to return
         * @param <xsl:text>arg_</xsl:text> <xsl:value-of select="$CName"/> element to set <xsl:value-of select="$nativePropertyType"/> to the array
         * @return ith <xsl:value-of select="$nativePropertyType"/> of the array
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i,
                <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>);


        /**
         * Add to <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param <xsl:text>arg_</xsl:text> <xsl:value-of select="$CName"/> element to add <xsl:value-of select="$nativePropertyType"/> to the array
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env,
                <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>);

        /**
         * Get the size of the <xsl:value-of select="$propertyName"/> array.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct.
         * @return the size of the <xsl:value-of select="$propertyName"/> array.
         */
        int AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env);

        /**
         * Remove the ith element of <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param i index of the item to remove
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_remove_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i);

        </xsl:if> <!-- xsl:if test="@isarray" -->
        </xsl:for-each> <!-- xsl:for-each select="property" -->

        <!-- The section covers the list types -->
        <xsl:for-each select="itemtype">
            <xsl:variable name="propertyType">axutil_array_list_t*</xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="$name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="$name"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:variable>
            <xsl:variable name="constValue">
                <xsl:choose>
                   <xsl:when test="@type='axis2_char_t*' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'">const </xsl:when>
                </xsl:choose>
            </xsl:variable>
         
        /**
         * Get the ith element of <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param i index of the item to return
         * @return ith <xsl:value-of select="$nativePropertyType"/> of the array
         */
        <xsl:value-of select="$nativePropertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i);

        /**
         * Set the ith element of <xsl:value-of select="$propertyName"/>. (If the ith already exist, it will be replaced)
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param i index of the item to return
         * @param <xsl:text>arg_</xsl:text> <xsl:value-of select="$CName"/> element to set <xsl:value-of select="$nativePropertyType"/> to the array
         * @return ith <xsl:value-of select="$nativePropertyType"/> of the array
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i,
                <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>);


        /**
         * Add to <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param <xsl:text>arg_</xsl:text> <xsl:value-of select="$CName"/> element to add <xsl:value-of select="$nativePropertyType"/> to the array
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env,
                <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>);

        /**
         * Get the size of the <xsl:value-of select="$propertyName"/> array.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct.
         * @return the size of the <xsl:value-of select="$propertyName"/> array.
         */
        int AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env);

        /**
         * Remove the ith element of <xsl:value-of select="$propertyName"/>.
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param i index of the item to remove
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_remove_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i);

   
        </xsl:for-each>

        <xsl:if test="$isEnum=1">
          <xsl:for-each select="property">
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
            <xsl:variable name="enum">adb_<xsl:value-of select="@name"/>_enum_t</xsl:variable>
            <xsl:variable name="constValue">
              <xsl:choose>
                <xsl:when test="@isarray"></xsl:when>
                <xsl:when test="@type='axis2_char_t*' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'">const </xsl:when>
              </xsl:choose>
            </xsl:variable>
            
            /************************** Getters and Setters For Enumerations ********************************/
            /********************* Enumeration Specific Operations: get_enum, set_enum **********************/
            
            /**
            * Enum getter for <xsl:value-of select="$propertyName"/>.
            * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
            * @param env pointer to environment struct
            * @return <xsl:value-of select="$enum"/>; -1 on failure
            */
            <xsl:value-of select="$enum"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_enum(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);
            
            /**
            * Enum setter for <xsl:value-of select="$propertyName"/>.
            * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
            * @param env pointer to environment struct
            * @param arg_<xsl:value-of select="$CName"/><xsl:text> </xsl:text> <xsl:value-of select="$enum"/>
            * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
            */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_enum(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            <xsl:value-of select="$constValue"/><xsl:value-of select="$enum"/><xsl:text> </xsl:text>arg_<xsl:value-of select="$CName"/>);
            
          </xsl:for-each>
        </xsl:if>


        /******************************* Checking and Setting NIL values *********************************/
        <xsl:if test="count(property[@array])!=0">/* Use 'Checking and Setting NIL values for Arrays' to check and set nil for individual elements */</xsl:if>

        /**
         * NOTE: set_nil is only available for nillable properties
         */

        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
            <xsl:choose>
                <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment">
                <xsl:choose>
                    <xsl:when test="@isarray"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$nativePropertyType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

        /**
         * Check whether <xsl:value-of select="$propertyName"/> is nill
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return AXIS2_TRUE if the element is nil or AXIS2_FALSE otherwise
         */
        axis2_bool_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_is_<xsl:value-of select="$CName"/>_nil(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env);


        <xsl:if test="@nillable or @optional">
        /**
         * Set <xsl:value-of select="$propertyName"/> to nill (currently the same as reset)
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env);
        </xsl:if>

        </xsl:for-each> <!-- for-each select="proprety" -->

        <xsl:if test="count(property[@array])!=0">
        /*************************** Checking and Setting 'NIL' values in Arrays *****************************/

        /**
         * NOTE: You may set this to remove specific elements in the array
         *       But you can not remove elements, if the specific property is declared to be non-nillable or sizeof(array) &lt; minOccurs
         */
        </xsl:if>

        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
            <xsl:choose>
                <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment">
                <xsl:choose>
                    <xsl:when test="@isarray"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$nativePropertyType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
        <xsl:if test="@isarray">
        /**
         * Check whether <xsl:value-of select="$propertyName"/> is nill at i
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct.
         * @param i index of the item to return.
         * @return AXIS2_TRUE if the element is nil or AXIS2_FALSE otherwise
         */
        axis2_bool_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_is_<xsl:value-of select="$CName"/>_nil_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i);
 
       
        /**
         * Set <xsl:value-of select="$propertyName"/> to nill at i
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> _<xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct.
         * @param i index of the item to set.
         * @return AXIS2_SUCCESS on success, or AXIS2_FAILURE otherwise.
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>, 
                const axutil_env_t *env, int i);

        </xsl:if> <!-- closes isarray -->
        </xsl:for-each>

        /**************************** Serialize and Deserialize functions ***************************/
        /*********** These functions are for use only inside the generated code *********************/

        <xsl:if test="@simple">
            /**
             * Deserialize the content from a string to adb objects
             * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/><xsl:text> </xsl:text> <xsl:value-of select="$axis2_name"/>_t object
             * @param env pointer to environment struct
             * @param node_value to deserialize
             * @param parent_element The parent element if it is an element, NULL otherwise
             * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_deserialize_from_string(
                            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                                            const axutil_env_t *env,
                                            const axis2_char_t *node_value,
                                            axiom_node_t *parent);
        </xsl:if>
        /**
         * Wrapper for the deserialization function, will invoke the extension mapper instead
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param dp_parent double pointer to the parent node to deserialize
         * @param dp_is_early_node_valid double pointer to a flag (is_early_node_valid?)
         * @param dont_care_minoccurs Dont set errors on validating minoccurs, 
         *              (Parent will order this in a case of choice)
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_deserialize(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            axiom_node_t** dp_parent,
            axis2_bool_t *dp_is_early_node_valid,
            axis2_bool_t dont_care_minoccurs);

        /**
         * Deserialize an XML to adb objects
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param dp_parent double pointer to the parent node to deserialize
         * @param dp_is_early_node_valid double pointer to a flag (is_early_node_valid?)
         * @param dont_care_minoccurs Dont set errors on validating minoccurs,
         *              (Parent will order this in a case of choice)
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_deserialize_obj(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            axiom_node_t** dp_parent,
            axis2_bool_t *dp_is_early_node_valid,
            axis2_bool_t dont_care_minoccurs);
                            
            <!-- Here the double pointer is used to change the parent pointer - This can be happned when deserialize is called in a particle class -->

       /**
         * Declare namespace in the most parent node 
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/><xsl:text> </xsl:text> <xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param parent_element parent element
         * @param namespaces hash of namespace uri to prefix
         * @param next_ns_index pointer to an int which contain the next namespace index
         */
       void AXIS2_CALL
       <xsl:value-of select="$axis2_name"/>_declare_parent_namespaces(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, axiom_element_t *parent_element,
                    axutil_hash_t *namespaces, int *next_ns_index);

        <xsl:if test="@simple">
        /**
         * Serialize to a String from the adb objects
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param namespaces hash of namespace uri to prefix
         * @return serialized string
         */
            axis2_char_t* AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_serialize_to_string(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, axutil_hash_t *namespaces);
        </xsl:if>

        /**
         * Wrapper for the serialization function, will invoke the extension mapper instead
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param <xsl:value-of select="$name"/>_om_node node to serialize from
         * @param <xsl:value-of select="$name"/>_om_element parent element to serialize from
         * @param tag_closed whether the parent tag is closed or not
         * @param namespaces hash of namespace uri to prefix
         * @param next_ns_index an int which contain the next namespace index
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_serialize(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            axiom_node_t* <xsl:value-of select="$name"/>_om_node, axiom_element_t *<xsl:value-of select="$name"/>_om_element, int tag_closed, axutil_hash_t *namespaces, int *next_ns_index);

        /**
         * Serialize to an XML from the adb objects
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param <xsl:value-of select="$name"/>_om_node node to serialize from
         * @param <xsl:value-of select="$name"/>_om_element parent element to serialize from
         * @param tag_closed whether the parent tag is closed or not
         * @param namespaces hash of namespace uri to prefix
         * @param next_ns_index an int which contain the next namespace index
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_serialize_obj(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            axiom_node_t* <xsl:value-of select="$name"/>_om_node, axiom_element_t *<xsl:value-of select="$name"/>_om_element, int tag_closed, axutil_hash_t *namespaces, int *next_ns_index);

        /**
         * Check whether the <xsl:value-of select="$axis2_name"/> is a particle class (E.g. group, inner sequence)
         * @return whether this is a particle class.
         */
        axis2_bool_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_is_particle();

        /******************************* Alternatives for Create and Free functions *********************************/

        <xsl:variable name="arg_list">
            <xsl:for-each select="property">
                <xsl:variable name="propertyType">
                <xsl:choose>
                    <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                    <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                    <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                </xsl:choose>
                </xsl:variable>
                <xsl:variable name="CName">_<xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
                <xsl:text>,
                </xsl:text><xsl:value-of select="$propertyType"/><xsl:text> </xsl:text><xsl:value-of select="$CName"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="arg_list_comment">
            <xsl:for-each select="property">
                <xsl:variable name="propertyType">
                <xsl:choose>
                    <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                    <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                    <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                </xsl:choose>
                </xsl:variable>
                <xsl:variable name="CName">_<xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
                <xsl:text>
         * @param </xsl:text><xsl:value-of select="$CName"/><xsl:text> </xsl:text><xsl:value-of select="$propertyType"/> 
            </xsl:for-each>
        </xsl:variable>

        /**
         * Constructor for creating <xsl:value-of select="$axis2_name"/>_t
         * @param env pointer to environment struct<xsl:value-of select="$arg_list_comment"/>
         * @return newly created <xsl:value-of select="$axis2_name"/>_t object
         */
        <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create_with_values(
            const axutil_env_t *env<xsl:value-of select="$arg_list"/>);

        <xsl:choose>
            <xsl:when test="count(property)">
                <xsl:variable name="firstProperty" select="property[1]"/>
                <xsl:variable name="propertyType">
                <xsl:choose>
                    <xsl:when test="$firstProperty/@isarray">axutil_array_list_t*</xsl:when>
                    <xsl:when test="not($firstProperty/@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                    <xsl:otherwise><xsl:value-of select="$firstProperty/@type"/></xsl:otherwise>
                </xsl:choose>
                </xsl:variable>
                <xsl:variable name="CName">_<xsl:value-of select="$firstProperty/@cname"></xsl:value-of></xsl:variable>


                /**
                 * Free <xsl:value-of select="$axis2_name"/>_t object and return the property value.
                 * You can use this to free the adb object as returning the property value. If there are
                 * many properties, it will only return the first property. Other properties will get freed with the adb object.
                 * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object to free
                 * @param env pointer to environment struct
                 * @return the property value holded by the ADB object, if there are many properties only returns the first.
                 */
                <xsl:value-of select="$propertyType"/> AXIS2_CALL
                <xsl:value-of select="$axis2_name"/>_free_popping_value(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                        const axutil_env_t *env);
            </xsl:when>
            <xsl:otherwise>

                /**
                 * Free <xsl:value-of select="$axis2_name"/>_t object and return the property value.
                 * You can use this to free the adb object as returning the property value. If there are
                 * many properties, it will only return the first property. Other properties will get freed with the adb object.
                 * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object to free
                 * @param env pointer to environment struct
                 * @return the property value holded by the ADB object, if there are many properties only returns the first.
                 */
                void* AXIS2_CALL
                <xsl:value-of select="$axis2_name"/>_free_popping_value(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
                        const axutil_env_t *env);
            </xsl:otherwise>
        </xsl:choose>

        /******************************* get the value by the property number  *********************************/
        /************NOTE: This method is introduced to resolve a problem in unwrapping mode *******************/

        <xsl:for-each select="property">
            <xsl:variable name="propertyType">
            <xsl:choose>
                <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
            </xsl:choose>
            </xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

            <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
               <xsl:choose>
                 <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                 <xsl:when test="@type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                 <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="paramComment">
                <xsl:choose>
                    <xsl:when test="@isarray"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$nativePropertyType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
        

        /**
         * Getter for <xsl:value-of select="$propertyName"/> by property number (<xsl:value-of select="position()"/>)
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return <xsl:value-of select="$paramComment"/>
         */
        <xsl:value-of select="$propertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_property<xsl:value-of select="position()"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

    </xsl:for-each>

    <xsl:for-each select="memberType">
        <xsl:variable name="member_type" select="@type"/>
        <xsl:variable name="member_name"><xsl:text></xsl:text><xsl:value-of select="@originalName"/></xsl:variable>

        /**
         * Getter for <xsl:value-of select="$member_name"/> 
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return <xsl:value-of select="$member_name"/>, if it the last set value
         */
        <xsl:value-of select="$member_type"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$member_name"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * Setter for <xsl:value-of select="$member_name"/> 
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @param member_type <xsl:value-of select="$member_name"/>
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env, 
            <xsl:value-of select="$member_type"/> member_type);

        /**
         * Check if the <xsl:value-of select="$member_name"/>  is valid, you can replace this with <xsl:value-of select="$axis2_name"/>_current_member_type
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return <xsl:value-of select="$member_name"/> is valid or not
         */
        axis2_bool_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_is_valid_<xsl:value-of select="$member_name"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

    </xsl:for-each>

    <xsl:if test="$isUnion">
        /**
         * Reset any value inside the union, this will take the union to NULL state
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_reset_members(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);

        /**
         * Retrieve the currrent member type
         * @param <xsl:text> _</xsl:text><xsl:value-of select="$name"/> <xsl:text> </xsl:text><xsl:value-of select="$axis2_name"/>_t object
         * @param env pointer to environment struct
         * @return axis2_char_t*, the current member type as a string
         */
        axis2_char_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_current_member_type(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> _</xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env);
    </xsl:if>
     #ifdef __cplusplus
     }
     #endif

     #endif /* <xsl:value-of select="$caps_axis2_name"/>_H */
    </xsl:template>

    <xsl:template match="mapper">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">axis2_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="caps_axis2_name">AXIS2_<xsl:value-of select="@caps-name"/></xsl:variable>
        #ifndef <xsl:value-of select="$caps_axis2_name"/>_H
        #define <xsl:value-of select="$caps_axis2_name"/>_H

        /**
         * <xsl:value-of select="$axis2_name"/>.h
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2/Java version: #axisVersion# #today#
         */

        #include &lt;stdio.h&gt;
        #include &lt;axiom.h&gt;
        #include &lt;axis2_util.h&gt;
        #include &lt;axiom_soap.h&gt;
        #include &lt;axis2_client.h&gt;

        #ifdef __cplusplus
        extern "C"
        {
        #endif

        typedef struct adb_type adb_type_t;

        /**
         * Returns the type for an ADB object.
         * @param object ADB object
         * @return string with the object type
         */
        axis2_char_t* AXIS2_CALL
        adb_type_get_type(const adb_type_t *object);

        /**
         * Will invoke the creation function for the appropriate type, given an Axiom node.
         * @param env pointer to environment struct
         * @param node double pointer to the parent node to deserialize
         * @param default_type string with the default type, in case the node has none
         * @return ADB object on success, else NULL
         */
        adb_type_t* AXIS2_CALL
        axis2_extension_mapper_create_from_node(
            const axutil_env_t *env,
            axiom_node_t** node,
            axis2_char_t *default_type);

        /**
         * Will invoke the "free" function for the appropriate type.
         * @param  _object ADB object
         * @param env pointer to environment struct
         * @param default_type string with the default type, in case the object has none
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        axis2_extension_mapper_free(
            adb_type_t* _object,
            const axutil_env_t *env,
            axis2_char_t *default_type);

        /**
         * Will invoke the deserialization function for the appropriate type.
         * @param  _object ADB object
         * @param env pointer to environment struct
         * @param dp_parent double pointer to the parent node to deserialize
         * @param dp_is_early_node_valid double pointer to a flag (is_early_node_valid?)
         * @param dont_care_minoccurs Dont set errors on validating minoccurs,
         *              (Parent will order this in a case of choice)
         * @param default_type string with the default type, in case the object has none
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axis2_status_t AXIS2_CALL
        axis2_extension_mapper_deserialize(
            adb_type_t* _object,
            const axutil_env_t *env,
            axiom_node_t** dp_parent,
            axis2_bool_t *dp_is_early_node_valid,
            axis2_bool_t dont_care_minoccurs,
            axis2_char_t *default_type);

        /**
         * Will invoke the serialization function for the appropriate type.
         * @param  _object ADB object
         * @param env pointer to environment struct
         * @param om_node node to serialize from
         * @param om_element parent element to serialize from
         * @param tag_closed whether the parent tag is closed or not
         * @param namespaces hash of namespace uri to prefix
         * @param next_ns_index an int which contain the next namespace index
         * @param default_type string with the default type, in case the object has none
         * @return AXIS2_SUCCESS on success, else AXIS2_FAILURE
         */
        axiom_node_t* AXIS2_CALL
        axis2_extension_mapper_serialize(
            adb_type_t* _object,
            const axutil_env_t *env,
            axiom_node_t* om_node,
            axiom_element_t *om_element,
            int tag_closed,
            axutil_hash_t *namespaces,
            int *next_ns_index,
            axis2_char_t *default_type);


        #ifdef __cplusplus
        }
        #endif

        #endif /* <xsl:value-of select="$caps_axis2_name"/>_H */
    </xsl:template>

</xsl:stylesheet>
