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

     <!-- cater for the multiple classes - wrappped mode - currently not well supported.-->
    <xsl:template match="/beans">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
        /**
         * <xsl:value-of select="$axis2_name"/>.c
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2/Java version: #axisVersion# #today#
         */
         
        #include "<xsl:value-of select="$axis2_name"/>.h"

        <xsl:apply-templates/>
    </xsl:template>
    <!--cater for the multiple classes - unwrappped mode -->
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="class">
        <xsl:variable name="name">_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="type_name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="just_name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">adb_<xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="istype"><xsl:value-of select="@type"/></xsl:variable>

        <xsl:variable name="originalName"><xsl:value-of select="@originalName"/></xsl:variable>
        <xsl:variable name="nsuri"><xsl:value-of select="@nsuri"/></xsl:variable>
        <xsl:variable name="nsprefix"><xsl:value-of select="@nsprefix"/></xsl:variable>
        <xsl:variable name="anon"><xsl:value-of select="@anon"/></xsl:variable>
        <xsl:variable name="ordered"><xsl:value-of select="@ordered"/></xsl:variable>
        <xsl:variable name="particleClass"><xsl:value-of select="@particleClass"/></xsl:variable> <!-- particle classes are used to represent schema groups -->
        <xsl:variable name="hasParticleType"><xsl:value-of select="@hasParticleType"/></xsl:variable> <!-- particle classes are used to represent schema groups -->
       
        <xsl:variable name="simple"><xsl:value-of select="@simple"/></xsl:variable>
        <xsl:variable name="choice"><xsl:value-of select="@choice"/></xsl:variable>

        <!-- Check if this type is a supported enum -->
        <xsl:variable name="isEnum">
          <xsl:choose>
            <xsl:when test="count(property)=1 and property/enumFacet and property/@type='axis2_char_t*'">1</xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <!-- checking for is union -->
        <xsl:variable name="isUnion" select="@union"/>

        /**
         * <xsl:value-of select="$axis2_name"/>.c
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2/C version: SNAPSHOT  Built on : Mar 10, 2008 (08:35:52 GMT+00:00)
         */

        #include "<xsl:value-of select="$axis2_name"/>.h"
        <xsl:choose>
            <xsl:when test="$istype">
                /*
                 * This type was generated from the piece of schema that had
                 * name = <xsl:value-of select="$originalName"/>
                 * Namespace URI = <xsl:value-of select="$nsuri"/>
                 * Namespace Prefix = <xsl:value-of select="$nsprefix"/>
                 */
           </xsl:when>
           <xsl:otherwise>
               /*
                * implmentation of the <xsl:value-of select="$originalName"/><xsl:if test="$nsuri">|<xsl:value-of select="$nsuri"/></xsl:if> element
                */
           </xsl:otherwise>
        </xsl:choose>


        struct <xsl:value-of select="$axis2_name"/>
        {
            axis2_char_t *property_Type;

            <xsl:if test="not($istype)">
                axutil_qname_t* qname;
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

                <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>property_<xsl:value-of select="$CName"/>;

                <!-- For arrays is_valid_* tracks for whether at least one element of the array is non-NULL -->
                <xsl:text>axis2_bool_t is_valid_</xsl:text><xsl:value-of select="$CName"/>;
            </xsl:for-each>

            <!-- The section covers the storage for list types, -->
            <xsl:for-each select="itemtype">
                <xsl:variable name="propertyType">axutil_array_list_t*</xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
                <xsl:variable name="CName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
 
                <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>property_<xsl:value-of select="$CName"/>;
                <xsl:text>axis2_bool_t is_valid_</xsl:text><xsl:value-of select="$CName"/>;

            </xsl:for-each>


            <xsl:if test="$choice">
                axis2_char_t *current_choice;
            </xsl:if>

            <!-- next it covers the union types -->
            <xsl:if test="$isUnion">
                /* for unions we are keeping members in a union */

                union {
                <xsl:for-each select="memberType">
                    <xsl:variable name="member_type" select="@type"/>
                    <xsl:variable name="name"><xsl:text>_</xsl:text><xsl:value-of select="@originalName"/></xsl:variable><xsl:text>
                    </xsl:text><xsl:value-of select="$member_type"/><xsl:text> </xsl:text><xsl:value-of select="$name"/>;
                </xsl:for-each>
                } member_type;
                
                /* here too we keep the choice */
                axis2_char_t *current_value;
            </xsl:if>
        };


       /************************* Private Function prototypes ********************************/
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
                   <xsl:when test="@type='unsigned char' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="paramComment">
                <xsl:choose>
                    <xsl:when test="@isarray"><xsl:text>Array of </xsl:text><xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text>s.</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:value-of select="$nativePropertyType"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:if test="not(@nillable or @optional)">
                <xsl:if test="@isarray">
                 axis2_status_t AXIS2_CALL
                 <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil_at(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>, 
                        const axutil_env_t *env, int i);
                </xsl:if>

                axis2_status_t AXIS2_CALL
                <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                        const axutil_env_t *env);
            </xsl:if>

          </xsl:for-each>


       /************************* Function Implmentations ********************************/
        <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create(
            const axutil_env_t *env)
        {
            <xsl:value-of select="$axis2_name"/>_t *<xsl:value-of select="$name"/> = NULL;
            <xsl:if test="not($istype)">
                axutil_qname_t* qname = NULL;
            </xsl:if>
            AXIS2_ENV_CHECK(env, NULL);

            <xsl:value-of select="$name"/> = (<xsl:value-of select="$axis2_name"/>_t *) AXIS2_MALLOC(env->
                allocator, sizeof(<xsl:value-of select="$axis2_name"/>_t));

            if(NULL == <xsl:value-of select="$name"/>)
            {
                AXIS2_ERROR_SET(env->error, AXIS2_ERROR_NO_MEMORY, AXIS2_FAILURE);
                return NULL;
            }

            memset(<xsl:value-of select="$name"/>, 0, sizeof(<xsl:value-of select="$axis2_name"/>_t));

            <xsl:value-of select="$name"/>->property_Type = axutil_strdup(env, "<xsl:value-of select="$axis2_name"></xsl:value-of>");
            <xsl:for-each select="property">
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
                <xsl:choose>
                  <xsl:when test="@ours or @type='axis2_char_t*' or @type='axutil_qname_t*' or @type='axutil_duration_t*' or @type='axutil_uri_t*' or @type='axutil_date_time_t*' or @type='axutil_base64_binary_t*'">
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>  = NULL;
                  </xsl:when>
                  <!-- todo for others -->
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>  = AXIS2_FALSE;
            </xsl:for-each>

            <xsl:if test="not($istype)">
              <xsl:choose>
                <xsl:when test="$nsuri and $nsuri != ''">
                  qname =  axutil_qname_create (env,
                        "<xsl:value-of select="$originalName"/>",
                        "<xsl:value-of select="$nsuri"/>",
                        NULL);
                </xsl:when>
                <xsl:otherwise>
                  qname =  axutil_qname_create (env,
                        "<xsl:value-of select="$originalName"/>",
                        NULL,
                        NULL);
                </xsl:otherwise>
              </xsl:choose>

              <xsl:value-of select="$name"/>->qname = qname;
            </xsl:if>

            <xsl:if test="$choice">
                <xsl:value-of select="$name"/>->current_choice = "";
            </xsl:if>
            <xsl:if test="$isUnion">
                <xsl:value-of select="$name"/>->current_value = "";
            </xsl:if>

            return <xsl:value-of select="$name"/>;
        }

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

        <!-- the following two functions was added in support of the unwrapped mode -->
        <xsl:value-of select="$axis2_name"/>_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_create_with_values(
            const axutil_env_t *env<xsl:value-of select="$arg_list"/>)
        {
            <xsl:value-of select="$axis2_name"/>_t* adb_obj = NULL;
            axis2_status_t status = AXIS2_SUCCESS;

            adb_obj = <xsl:value-of select="$axis2_name"/>_create(env);

            <xsl:for-each select="property">
              <xsl:variable name="CName">_<xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
              status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="@cname"/>(
                                     adb_obj,
                                     env,
                                     <xsl:value-of select="$CName"/>);
              if(status == AXIS2_FAILURE) {
                  <xsl:value-of select="$axis2_name"/>_free (adb_obj, env);
                  return NULL;
              }
            </xsl:for-each>
           
            <xsl:if test="@isUnion">
            /* this function is not implemented for union types */
            </xsl:if>

            return adb_obj;
        }
      
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
                <xsl:variable name="CName"><xsl:value-of select="$firstProperty/@cname"></xsl:value-of></xsl:variable>

                <xsl:value-of select="$propertyType"/> AXIS2_CALL
                <xsl:value-of select="$axis2_name"/>_free_popping_value(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                        const axutil_env_t *env)
                {
                    <xsl:value-of select="$propertyType"/> value;

                    <xsl:if test="@isUnion">
                    /* this function is not completely implemented for union types */
                    </xsl:if>
                    
                    value = <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>;

                    <xsl:if test="$firstProperty/@ours or $firstProperty/@isarray or $propertyType='axis2_char_t*' or $propertyType='axutil_qname_t*' or $propertyType='axutil_duration_t*' or $propertyType='axutil_uri_t*' or $propertyType='axutil_date_time_t*' or $propertyType='axutil_base64_binary_t*'">
                      <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = (<xsl:value-of select="$propertyType"/>)NULL;
                    </xsl:if>
                    <xsl:value-of select="$axis2_name"/>_free(<xsl:value-of select="$name"/>, env);

                    return value;
                }
            </xsl:when>
            <xsl:otherwise>
                void* AXIS2_CALL
                <xsl:value-of select="$axis2_name"/>_free_popping_value(
                        <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                        const axutil_env_t *env)
                {
                    <xsl:value-of select="$axis2_name"/>_free(<xsl:value-of select="$name"/>, env);
                    return NULL;
                }
            </xsl:otherwise>

        </xsl:choose>

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env)
        {
            <!-- Only use the extension mapper for actual types -->
            <xsl:choose>
            <xsl:when test="@type = 'yes'">
            return axis2_extension_mapper_free(
                (adb_type_t*) <xsl:value-of select="$name"/>,
                env,
                "<xsl:value-of select="$axis2_name"/>");
            </xsl:when>
            <xsl:otherwise>
            return <xsl:value-of select="$axis2_name"/>_free_obj(
                <xsl:value-of select="$name"/>,
                env);
            </xsl:otherwise>
            </xsl:choose>
        }

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_free_obj(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env)
        {
            <xsl:if test="property/@isarray">
                int i = 0;
                int count = 0;
                void *element = NULL;
            </xsl:if>

            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
            AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

            if (<xsl:value-of select="$name"/>->property_Type != NULL)
            {
              AXIS2_FREE(env->allocator, <xsl:value-of select="$name"/>->property_Type);
            }

            <xsl:for-each select="property">
                <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
                <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);
            </xsl:for-each>

            <!-- The section covers the list types -->
            <xsl:for-each select="itemtype">
                <xsl:variable name="CName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
                <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);
            </xsl:for-each>

            <xsl:if test="$isUnion">
            <xsl:value-of select="$axis2_name"/>_reset_members(<xsl:value-of select="$name"/>, env);
            </xsl:if>

            <xsl:if test="not(@type)">
              if(<xsl:value-of select="$name"/>->qname)
              {
                  axutil_qname_free (<xsl:value-of select="$name"/>->qname, env);
                  <xsl:value-of select="$name"/>->qname = NULL;
              }
            </xsl:if>

            if(<xsl:value-of select="$name"/>)
            {
                AXIS2_FREE(env->allocator, <xsl:value-of select="$name"/>);
                <xsl:value-of select="$name"/> = NULL;
            }

            return AXIS2_SUCCESS;
        }


        <xsl:if test="@simple">
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_deserialize_from_string(
                            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                                            const axutil_env_t *env,
                                            const axis2_char_t *node_value,
                                            axiom_node_t *parent)
            {
              axis2_status_t status = AXIS2_SUCCESS;
            <xsl:if test="property/@type='axutil_date_time_t*' or property/@type='axutil_base64_binary_t*'">
              void *element = NULL;
            </xsl:if>
            <xsl:if test="itemtype/@type='axutil_date_time_t*' or itemtype/@type='axutil_base64_binary_t*' or @ours">
              void *element = NULL;
            </xsl:if>
            <xsl:if test="property/@type='axutil_qname_t*' or itemtype/@type='axutil_qname_t*'">
              const axis2_char_t *cp_ro = NULL;
              axis2_bool_t prefix_found = AXIS2_FALSE;
              axiom_namespace_t *qname_ns;
            </xsl:if>

            <xsl:if test="itemtype">
               int i;
               axis2_char_t *token_value = NULL;
               axis2_char_t *original_node_value = NULL;
               axis2_char_t *dupd_node_value = NULL;
               axis2_bool_t the_last_token = AXIS2_FALSE;
            </xsl:if>

              <xsl:for-each select="property"> <!-- only one property would be in a simpletype -->
                <xsl:if test="position()=1"> <!-- just to make sure it doesn't go for more than one time -->
                  <xsl:variable name="propertyType">
                     <xsl:choose>
                       <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
                  <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>
    
                  <!-- here only simple types possible -->
                  <xsl:choose>
                    <!-- add int s -->
                    <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, atoi(node_value));
                    </xsl:when>

                    <!-- add axis2_char_t s -->
                    <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, (char)(*node_value)); <!-- This should be checked -->
                    </xsl:when>

                    <!-- add short s -->
                    <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, atoi(node_value));
                    </xsl:when>

                    <!-- add int64_t s -->
                    <xsl:when test="$nativePropertyType='int64_t'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, axutil_strtol(node_value, (char**)NULL, 0));
                    </xsl:when>
                    <xsl:when test="$nativePropertyType='uint64_t'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, axutil_strtoul(node_value, (char**)NULL, 0));
                    </xsl:when>

                    <!-- add float s -->
                    <xsl:when test="$nativePropertyType='float'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, atof(node_value));
                    </xsl:when>
                    <!-- add double s -->
                    <xsl:when test="$nativePropertyType='double'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, atof(node_value));
                    </xsl:when>

                    <!-- add axis2_char_t s -->
                    <xsl:when test="$nativePropertyType='axis2_char_t*'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, node_value);
                    </xsl:when>

                    <!-- add axutil_qname_t s -->
                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">

                        prefix_found = AXIS2_FALSE;
                        for(cp_ro = node_value; *cp_ro; cp_ro ++)
                        {
                            if(*cp_ro == ':')
                            {
                                cp_ro ++;
                                prefix_found  = AXIS2_TRUE;
                                break;
                            }
                        }

                        if(prefix_found)
                        {
                            /* node value contain the prefix */
                            char *prefix_value = AXIS2_MALLOC(env->allocator, (cp_ro - node_value - 1) + 1);
                            strncpy(prefix, node_value, (cp_ro - node_value - 1));
                            prefix[cp_ro - node_value - 1] = '\0';
                            qname_ns = axiom_element_find_namespace_uri((axiom_element_t*)axiom_node_get_data_element(parent, env), env, prefix_value, parent);
                            AXIS2_FREE(env->allocator, prefix_value);
                        }
                        else
                        {
                            /* Then it is the default namespace */
                            cp_ro = node_value;
                            qname_ns = axiom_element_get_default_namespace((axiom_element_t*)axiom_node_get_data_element(parent, env), env, parent);
                        }

                         <!-- we are done extracting info, just set the extracted value to the qname -->

                         <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                        env,
                                                        axutil_qname_create(
                                                              env, 
                                                              cp_ro, /* cp contain the localname */
                                                              axiom_namespace_get_uri(qname_ns, env),
                                                              axiom_namespace_get_prefix(qname_ns, env)));
                      </xsl:when>



                    <!-- add axutil_uri_t s -->
                    <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, axutil_uri_parse_string(env, node_value));
                    </xsl:when>

                    <!-- add axutil_duration_t s -->
                    <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, axutil_duration_create_from_string(env, node_value));
                    </xsl:when>

                    <!-- add axis2_bool_t s -->
                    <xsl:when test="$nativePropertyType='axis2_bool_t'">
                       if (!axutil_strcmp(node_value, "TRUE") || !axutil_strcmp(node_value, "true"))
                       {
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, AXIS2_TRUE);
                       }
                       else
                       {
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, AXIS2_FALSE);
                       }
                    </xsl:when>
                    <!-- add axis2_byte_t s -->
                    <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, atoi(node_value));
                    </xsl:when>
                    <!-- add date_time_t* s -->
                    <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                       element = (void*)axutil_date_time_create(env);
                       axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                  node_value);
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, (<xsl:value-of select="$nativePropertyType"/>)element);
                    </xsl:when>
                    <!-- add hex_binary_t* s -->
                    <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                       element = (void*)axutil_base64_binary_create(env);
                       axutil_base64_binary_set_encoded_binary((<xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                  node_value);
                       <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                      env, (<xsl:value-of select="$nativePropertyType"/>)element);
                    </xsl:when>
                    <xsl:when test="@ours">
                      element =  (void*)<xsl:value-of select="substring-before($nativePropertyType, '_t*')"/>_create(env);
                      <xsl:value-of select="substring-before($nativePropertyType, '_t*')"/>_deserialize_from_string((<xsl:value-of select="$nativePropertyType"/>)element, env, attrib_text, parent);
                      <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                (<xsl:value-of select="$nativePropertyType"/>)element);
                    </xsl:when>
                    <xsl:otherwise>
                       <!--TODO: add new attributes types -->
                       /* can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                       status = AXIS2_FAILURE;
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:if>
              </xsl:for-each>

            <!-- The section covers the list types, this is a loop always occurs just 1 time-->
            <xsl:for-each select="itemtype">
                <xsl:variable name="propertyType">axutil_array_list_t*</xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
                <xsl:variable name="CName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
 
                <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                   <xsl:choose>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                  <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                     <xsl:choose>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:when test="@type='unsigned char' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                
                <xsl:variable name="propertyInstanceName">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:variable>
                <xsl:variable name="justPropertyInstanceName">element</xsl:variable>
                
                /* just to make sure we are not altering the original */
                dupd_node_value = original_node_value = (axis2_char_t*)axutil_strdup(env, node_value);

                for(token_value = dupd_node_value, the_last_token = AXIS2_FALSE; !the_last_token; dupd_node_value ++)
                {
                    if(*dupd_node_value == ' ' || *dupd_node_value == '\t' || *dupd_node_value == '\r'
                            || *dupd_node_value == '\n' || *dupd_node_value == '\0')
                    {
                        if(*dupd_node_value == '\0')
                        {
                            the_last_token = AXIS2_TRUE;
                        }
                        else
                        {
                            *dupd_node_value = '\0';
                        }
                        

                <xsl:choose>
                  <!-- add int s -->
                  <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(token_value));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, (char)(*token_value)); <!-- This should be checked -->
                  </xsl:when>

                  <!-- add short s -->
                  <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(token_value));
                  </xsl:when>

                  <!-- add int64_t s -->
                  <xsl:when test="$nativePropertyType='int64_t'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_strtol(token_value, (char**)NULL, 0));
                  </xsl:when>
                  <xsl:when test="$nativePropertyType='uint64_t'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_strtoul(token_value, (char**)NULL, 0));
                  </xsl:when>

                  <!-- add float s -->
                  <xsl:when test="$nativePropertyType='float'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atof(token_value));
                  </xsl:when>
                  <!-- add double s -->
                  <xsl:when test="$nativePropertyType='double'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atof(token_value));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$nativePropertyType='axis2_char_t*'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, token_value);
                  </xsl:when>

                  <!-- add axutil_qname_t s -->
                  <xsl:when test="$nativePropertyType='axutil_qname_t*'">

                    prefix_found = AXIS2_FALSE;
                    for(cp = token_value; *cp; cp ++)
                    {
                        if(*cp == ':')
                        {
                            *cp = '\0';
                            cp ++;
                            prefix_found  = AXIS2_TRUE;
                            break;
                        }
                    }

                    if(prefix_found)
                    {
                        /* node value contain the prefix */
                        qname_ns = axiom_element_find_namespace_uri((axiom_element_t*)axiom_node_get_data_element(parent, env), env, token_value, parent);
                    }
                    else
                    {
                        /* Then it is the default namespace */
                        cp = token_value;
                        qname_ns = axiom_element_get_default_namespace((axiom_element_t*)axiom_node_get_data_element(parent, env), env, parent);
                    }

                     <!-- we are done extracting info, just set the extracted value to the qname -->

                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env,
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                  </xsl:when>

                 <!-- add axutil_uri_t s -->
                  <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_uri_parse_string(env, token_value));
                  </xsl:when>

                  <!-- add axutil_duration_t s -->
                  <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_duration_create_from_string(env, token_value));
                  </xsl:when>

                  <!-- add axis2_bool_t s -->
                  <xsl:when test="$nativePropertyType='axis2_bool_t'">
                     if (!axutil_strcmp(token_value, "TRUE") || !axutil_strcmp(token_value, "true"))
                     {
                         <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, AXIS2_TRUE);
                     }
                     else
                     {
                         <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, AXIS2_FALSE);
                     }
                  </xsl:when>
                  <!-- add axis2_byte_t s -->
                  <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(token_value));
                  </xsl:when>
                  <!-- add date_time_t* s -->
                  <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                     element = (void*)axutil_date_time_create(env);
                     axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                token_value);
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, (<xsl:value-of select="$nativePropertyType"/>)element);
                  </xsl:when>
                  <!-- add hex_binary_t* s -->
                  <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                     element = (void*)axutil_base64_binary_create(env);
                     axutil_base64_binary_set_encoded_binary((<xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                token_value);
                     <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env, (<xsl:value-of select="$nativePropertyType"/>)element);
                  </xsl:when>
                  <xsl:when test="@ours">
                      element =  (void*)<xsl:value-of select="substring-before($nativePropertyType, '_t*')"/>_create(env);
                      <xsl:value-of select="substring-before($nativePropertyType, '_t*')"/>_deserialize_from_string((<xsl:value-of select="$nativePropertyType"/>)element, env, attrib_text, parent);
                      <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                (<xsl:value-of select="$nativePropertyType"/>)element);
                  </xsl:when>
                  <xsl:otherwise>
                     <!--TODO: add new attributes types -->
                     /* can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                     status = AXIS2_FAILURE;
                  </xsl:otherwise>
                </xsl:choose>   
                    token_value = dupd_node_value + 1;
                  }
              }
              AXIS2_FREE(env->allocator, original_node_value);

             </xsl:for-each>

             <xsl:if test="$isUnion">
             
               /*
                * axis2_qname_t *qname = NULL;
                * axiom_attribute_t *the_attri = NULL;
                * 
                * qname = axutil_qname_create(env, "type", "http://www.w3.org/2001/XMLSchema-instance", "xsi");
                * the_attri = axiom_element_get_attribute(current_element, env, qname);
                */
               /* currently thereis a bug in the axiom_element_get_attribute, so we have to go to this bad method */

               axiom_attribute_t *the_attri = NULL;
               axis2_char_t *attrib_text = NULL;
               axutil_hash_t *attribute_hash = NULL;
               void *element = NULL;
               axiom_element_t *current_element = NULL;

               current_element = (axiom_element_t*)axiom_node_get_data_element(parent, env);

               attribute_hash = axiom_element_get_all_attributes(current_element, env);

               attrib_text = NULL;
               if(attribute_hash)
               {
                    axutil_hash_index_t *hi;
                    void *val;
                    const void *key;
           
                    for (hi = axutil_hash_first(attribute_hash, env); hi; hi = axutil_hash_next(env, hi)) 
                    {
                        axutil_hash_this(hi, &amp;key, NULL, &amp;val);
                        
                        if(strstr((axis2_char_t*)key, "type|http://www.w3.org/2001/XMLSchema-instance"))
                        {
                            the_attri = (axiom_attribute_t*)val;
                            break;
                        }
                    }
               }

               if(the_attri)
               {
                   attrib_text = axiom_attribute_get_value(the_attri, env);
               }
               else
               {
                   /* this is hoping that attribute is stored in "http://www.w3.org/2001/XMLSchema-instance", this happnes when name is in default namespace */
                   attrib_text = axiom_element_get_attribute_value_by_name(current_element, env, "type");
               }

               if(attrib_text)
               {
                    /* skipping the namespace prefix */
                    axis2_char_t *temp_attrib = NULL;
                    temp_attrib = strchr(attrib_text, ':');
                    if(temp_attrib)
                    {
                        /* take the string after the ':' character */
                        attrib_text = temp_attrib + 1;
                    }
               }

               if(!attrib_text) {
                    /* nothing is here, reset things */
                    status = <xsl:value-of select="$axis2_name"/>_reset_members(<xsl:value-of select="$name"/>, env);
               }
             <xsl:for-each select="memberType">
                <xsl:variable name="member_type" select="@type"/>
                <xsl:variable name="member_name"><xsl:text></xsl:text><xsl:value-of select="@originalName"/></xsl:variable>   
               else if(!axutil_strcmp(attrib_text, "<xsl:value-of select="@originalName"/>"))
               {

                <xsl:choose>
                  <!-- add int s -->
                  <xsl:when test="$member_type='int' or $member_type='unsigned int'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(attrib_text));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$member_type='char' or $member_type='unsigned char'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, (char)(*attrib_text)); <!-- This should be checked -->
                  </xsl:when>

                  <!-- add short s -->
                  <xsl:when test="$member_type='short' or $member_type='unsigned short'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(attrib_text));
                  </xsl:when>

                  <!-- add int64_t s -->
                  <xsl:when test="$member_type='int64_t'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_strtol(attrib_text, (char**)NULL, 0));
                  </xsl:when>
                  <xsl:when test="$member_type='uint64_t'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_strtoul(attrib_text, (char**)NULL, 0));
                  </xsl:when>

                  <!-- add float s -->
                  <xsl:when test="$member_type='float'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, atof(attrib_text));
                  </xsl:when>
                  <!-- add double s -->
                  <xsl:when test="$member_type='double'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, atof(attrib_text));
                  </xsl:when>

                  <!-- add axis2_char_t s -->
                  <xsl:when test="$member_type='axis2_char_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, attrib_text);
                  </xsl:when>

                  <!-- add axutil_qname_t s -->
                  <xsl:when test="$member_type='axutil_qname_t*'">

                    prefix_found = AXIS2_FALSE;
                    for(cp = attrib_text; *cp; cp ++)
                    {
                        if(*cp == ':')
                        {
                            *cp = '\0';
                            cp ++;
                            prefix_found  = AXIS2_TRUE;
                            break;
                        }
                    }

                    if(prefix_found)
                    {
                        /* node value contain the prefix */
                        qname_ns = axiom_element_find_namespace_uri((axiom_element_t*)axiom_node_get_data_element(parent, env), env, attrib_text, parent);
                    }
                    else
                    {
                        /* Then it is the default namespace */
                        cp = attrib_text;
                        qname_ns = axiom_element_get_default_namespace((axiom_element_t*)axiom_node_get_data_element(parent, env), env, parent);
                    }

                     <!-- we are done extracting info, just set the extracted value to the qname -->

                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env,
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                  </xsl:when>

                 <!-- add axutil_uri_t s -->
                  <xsl:when test="$member_type='axutil_uri_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_uri_parse_string(env, attrib_text));
                  </xsl:when>

                  <!-- add axutil_duration_t s -->
                  <xsl:when test="$member_type='axutil_duration_t*'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, axutil_duration_create_from_string(env, attrib_text));
                  </xsl:when>

                  <!-- add axis2_bool_t s -->
                  <xsl:when test="$member_type='axis2_bool_t'">
                     if (!axutil_strcmp(attrib_text, "TRUE") || !axutil_strcmp(token_value, "true"))
                     {
                         <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, AXIS2_TRUE);
                     }
                     else
                     {
                         <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, AXIS2_FALSE);
                     }
                  </xsl:when>
                  <!-- add axis2_byte_t s -->
                  <xsl:when test="$member_type='axis2_byte_t' or $member_type='axis2_unsigned_byte_t'">
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, atoi(attrib_text));
                  </xsl:when>
                  <!-- add date_time_t* s -->
                  <xsl:when test="$member_type='axutil_date_time_t*'">
                     element = (void*)axutil_date_time_create(env);
                     axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                attrib_text);
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, (<xsl:value-of select="$member_type"/>)element);
                  </xsl:when>
                  <!-- add hex_binary_t* s -->
                  <xsl:when test="$member_type='axutil_base64_binary_t*'">
                     element = (void*)axutil_base64_binary_create(env);
                     axutil_base64_binary_set_encoded_binary((<xsl:value-of select="$member_type"/>)element, env,
                                                                attrib_text);
                     <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>,
                                                    env, (<xsl:value-of select="$member_type"/>)element);
                  </xsl:when>
                  <xsl:when test="@ours">
                      element =  (void*)<xsl:value-of select="substring-before($member_type, '_t*')"/>_create(env);
                      <xsl:value-of select="substring-before($member_type, '_t*')"/>_deserialize_from_string((<xsl:value-of select="$member_type"/>)element, env, attrib_text, parent);
                      <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(<xsl:value-of select="$name"/>, env,
                                                                (<xsl:value-of select="$member_type"/>)element);
                  </xsl:when>
                  <xsl:otherwise>
                     <!--TODO: add new attributes types -->
                     /* can not handle the attribute type <xsl:value-of select="$member_type"/>*/
                     status = AXIS2_FAILURE;
                  </xsl:otherwise>
                </xsl:choose>   

               }
             </xsl:for-each>
             </xsl:if>
              return status;
            }
        </xsl:if>

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_deserialize(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env,
                axiom_node_t **dp_parent,
                axis2_bool_t *dp_is_early_node_valid,
                axis2_bool_t dont_care_minoccurs)
        {
            <!-- Only use the extension mapper for actual types -->
            <xsl:choose>
            <xsl:when test="@type = 'yes'">
            return axis2_extension_mapper_deserialize(
                (adb_type_t*) <xsl:value-of select="$name"/>,
                env,
                dp_parent,
                dp_is_early_node_valid,
                dont_care_minoccurs,
                "<xsl:value-of select="$axis2_name"/>");
            </xsl:when>
            <xsl:otherwise>
            return <xsl:value-of select="$axis2_name"/>_deserialize_obj(
                <xsl:value-of select="$name"/>,
                env,
                dp_parent,
                dp_is_early_node_valid,
                dont_care_minoccurs);
            </xsl:otherwise>
            </xsl:choose>
        }

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_deserialize_obj(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env,
                axiom_node_t **dp_parent,
                axis2_bool_t *dp_is_early_node_valid,
                axis2_bool_t dont_care_minoccurs)
        {
          axiom_node_t *parent = *dp_parent;
          
          axis2_status_t status = AXIS2_SUCCESS;
          <xsl:if test="count(property[@attribute])!=0">
              axiom_attribute_t *parent_attri = NULL;
              axiom_element_t *parent_element = NULL;
              axis2_char_t *attrib_text = NULL;

              axutil_hash_t *attribute_hash = NULL;

          </xsl:if>
          <xsl:if test="property/@ours or (property/@isarray and (property/@type='unsigned short' or property/@type='unsigned char' or property/@type='unsigned int' or property/@type='uint64_t' or property/@type='short' or property/@type='int' or property/@type='char' or property/@type='int64_t' or property/@type='float' or property/@type='axis2_unsigned_byte_t' or property/@type='axis2_byte_t' or property/@type='axis2_bool_t' or property/@type='double')) or property/@type='axutil_date_time_t*' or property/@type='axutil_base64_binary_t*'">
              void *element = NULL;
          </xsl:if>

          <!-- these two are requried -->
          <xsl:if test="count(property)!=0  or count(itemtype)!=0 or $isUnion"> <!-- check for at least one element exists -->
             const axis2_char_t* text_value = NULL;
             axutil_qname_t *qname = NULL;
          </xsl:if>

          <!-- qname specifc values -->
            <xsl:if test="property/@type='axutil_qname_t*'">
              axis2_char_t *cp = NULL;
              axis2_bool_t prefix_found = AXIS2_FALSE;
              axiom_namespace_t *qname_ns;
            </xsl:if>
          <xsl:choose>
            <xsl:when test="@simple and (count(property)!=0 or count(itemtype)!=0 or $isUnion)">
            
            status = AXIS2_FAILURE;
            if(parent)
            {
                axis2_char_t *attrib_text = NULL;
                attrib_text = axiom_element_get_attribute_value_by_name(axiom_node_get_data_element(parent, env), env, "nil");
                if (attrib_text != NULL &amp;&amp; !axutil_strcasecmp(attrib_text, "true"))
                {
                  <xsl:choose>
                   <xsl:when test="not(@nillable)">
                   /* but the wsdl says that, this is non nillable */
                    AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$originalName"/>");
                    status = AXIS2_FAILURE;
                   </xsl:when>
                   <xsl:otherwise>
                    status = <xsl:value-of select="$axis2_name"/>_set<xsl:value-of select="$name"/>_nil(<xsl:value-of select="$name"/>, env);
                   </xsl:otherwise>
                  </xsl:choose>
                }
                else
                {
                    axiom_node_t *text_node = NULL;
                    axiom_text_t *text_element = NULL;
                    text_node = axiom_node_get_first_child(parent, env);
                    if (text_node &amp;&amp;
                            axiom_node_get_node_type(text_node, env) == AXIOM_TEXT)
                        text_element = (axiom_text_t*)axiom_node_get_data_element(text_node, env);
                    text_value = "";
                    if(text_element &amp;&amp; axiom_text_get_value(text_element, env))
                    {
                        text_value = (axis2_char_t*)axiom_text_get_value(text_element, env);
                    }
                    status = <xsl:value-of select="$axis2_name"/>_deserialize_from_string(<xsl:value-of select="$name"/>, env, text_value, parent);
                }
            }
            </xsl:when>
            <xsl:otherwise>

            <xsl:if test="property/@isarray">
               int i = 0;
               axutil_array_list_t *arr_list = NULL;
            </xsl:if>
            <xsl:if test="(@ordered or @choice) and property/@isarray">
               int sequence_broken = 0;
               axiom_node_t *tmp_node = NULL;
            </xsl:if>
            <xsl:variable name="element_qname_var_requred">
                  <xsl:for-each select="property">
                    <xsl:if test="(not(@attribute) and @isarray) or not($ordered)">
                        yes
                    </xsl:if>
                  </xsl:for-each>
            </xsl:variable>
            <xsl:if test="contains($element_qname_var_requred, 'yes')">
                 <!-- TODO axutil_qname_t *element_qname = NULL; -->
            </xsl:if>
            axutil_qname_t *element_qname = NULL; 
            <xsl:if test="count(property)!=0">
               axiom_node_t *first_node = NULL;
               axis2_bool_t is_early_node_valid = AXIS2_TRUE;
               axiom_node_t *current_node = NULL;
               axiom_element_t *current_element = NULL;
            </xsl:if>
            AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
            AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

            <xsl:if test="property">
              <!-- We are expected to have NULL elements in particle classes -->
              <xsl:if test="not($particleClass)">
              <!-- Wait until AXIOM_ELEMENT -->
              while(parent &amp;&amp; axiom_node_get_node_type(parent, env) != AXIOM_ELEMENT)
              {
                  parent = axiom_node_get_next_sibling(parent, env);
              }
              if (NULL == parent)
              {
                /* This should be checked before everything */
                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, 
                            "Failed in building adb object for <xsl:value-of select="$originalName"/> : "
                            "NULL element can not be passed to deserialize");
                return AXIS2_FAILURE;
              }
              </xsl:if>
            </xsl:if>
            <xsl:for-each select="property">
              <xsl:if test="position()=1"> <!-- check for at least one element exists -->
                 <xsl:choose>
                    <xsl:when test="not($istype)">

                    current_element = (axiom_element_t *)axiom_node_get_data_element(parent, env);
                    qname = axiom_element_get_qname(current_element, env, parent);
                    if (axutil_qname_equals(qname, env, <xsl:value-of select="$name"/>-> qname)<xsl:if test="not($nsuri) or $nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$originalName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)
                    {
                        <xsl:choose>
                          <xsl:when test="$anon">
                          first_node = axiom_node_get_first_child(parent, env);
                          </xsl:when>
                          <xsl:otherwise>
                          first_node = parent;
                          </xsl:otherwise>
                        </xsl:choose>
                    }
                    else
                    {
                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, 
                              "Failed in building adb object for <xsl:value-of select="$originalName"/> : "
                              "Expected %s but returned %s",
                              axutil_qname_to_string(<xsl:value-of select="$name"/>-> qname, env),
                              axutil_qname_to_string(qname, env));
                        <!-- TODO: ADB specific error should be defined and set here -->
                        return AXIS2_FAILURE;
                    }
                    </xsl:when>
                    <xsl:when test="$particleClass">
                         first_node = parent;
                    </xsl:when>
                    <xsl:otherwise>
                      <!-- for types, parent refers to the container element -->
                      first_node = axiom_node_get_first_child(parent, env);
                      <!-- Let followers to check the situation
                      if(first_node == NULL)
                      {
                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI,
                                            "Failed in building adb object for <xsl:value-of select="$originalName"/> : "
                                            "It is expected to have a child element");
                          TODO: ADB specific error should be defined and set here 
                          return AXIS2_FAILURE; 
                      } 
                      -->
                    </xsl:otherwise>
                 </xsl:choose>
               </xsl:if>
            </xsl:for-each>
            
            </xsl:otherwise> <!--otherwise for @simple check -->
          </xsl:choose>

          <!-- attributes are common to simple types(when used in simple content) and other types -->
            <xsl:for-each select="property/@attribute">
              <xsl:if test="position()=1">
                 parent_element = (axiom_element_t *)axiom_node_get_data_element(parent, env);
                 attribute_hash = axiom_element_get_all_attributes(parent_element, env);
              </xsl:if>
            </xsl:for-each>

            <xsl:for-each select="property">
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="parentPropertyInstanceName"><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:variable>
              <xsl:choose>
                <xsl:when test="@attribute">
                <!-- here we have two options, either it can be axiom_attribute_t* which happens in anyAttribute case -->
                <xsl:choose>
                <xsl:when test="$nativePropertyType='axiom_attribute_t*' and @isarray">
                  parent_attri = NULL;
                  attrib_text = NULL;
                  if(attribute_hash)
                  {
                       axutil_hash_index_t *hi;
                       void *val;
                       const void *key;
                       axis2_char_t *dup_key;


                       char *seperator = NULL;
                       axis2_char_t *uri = NULL;
                       axiom_namespace_t *namespace = NULL;

                       axiom_attribute_t *new_attrib = NULL;

                       for (hi = axutil_hash_first(attribute_hash, env); hi; hi = axutil_hash_next(env, hi)) 
                       {
                           axutil_hash_this(hi, &amp;key, NULL, &amp;val);
                          
                           dup_key = axutil_strdup(env, key);
                           seperator = strstr(dup_key, "|");
                          
                           uri = NULL;
                           if(seperator) /* this means the attribute is qualified with a namespace */
                           {
                             *seperator = '\0';
                             seperator ++; /* represent the namespace */
                             uri = seperator;
                           }

                           namespace  = axiom_namespace_create(env, uri, NULL);
                           parent_attri = (axiom_attribute_t*)val;
                           attrib_text = axiom_attribute_get_value(parent_attri, env);

                           new_attrib = axiom_attribute_create(env, dup_key, attrib_text, namespace);


                           <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, new_attrib);
                           AXIS2_FREE(env->allocator, dup_key);
                       }
                  }
                </xsl:when>
                <xsl:otherwise>
                <!-- Just waiting for fix the axiom_element_get_attribute 
                  <xsl:choose>
                    <xsl:when test="@nsuri and @nsuri != ''">
                      qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                    </xsl:when>
                    <xsl:otherwise>
                      qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                    </xsl:otherwise>
                  </xsl:choose>

                  parent_attri = axiom_element_get_attribute(parent_element, env, qname);
                  if(parent_attri != NULL)
                  {
                    attrib_text = axiom_attribute_get_value(parent_attri, env);
                  }
                  else
                  {
                    attrib_text = axiom_element_get_attribute_value_by_name(parent_element, env, "<xsl:value-of select="$propertyName"/>");
                  }
                  if(qname)
                  {
                     axutil_qname_free(qname, env);
                  } -->
                
                  parent_attri = NULL;
                  attrib_text = NULL;
                  if(attribute_hash)
                  {
                       axutil_hash_index_t *hi;
                       void *val;
                       const void *key;

                       for (hi = axutil_hash_first(attribute_hash, env); hi; hi = axutil_hash_next(env, hi)) 
                       {
                           axutil_hash_this(hi, &amp;key, NULL, &amp;val);
                           
                           <xsl:choose>
                             <xsl:when test="@nsuri and @nsuri != ''">
                               if(strstr((axis2_char_t*)key, "<xsl:value-of select="$propertyName"/>|<xsl:value-of select="@nsuri"/>"))
                             </xsl:when>
                             <xsl:otherwise>
                               if(!strcmp((axis2_char_t*)key, "<xsl:value-of select="$propertyName"/>"))
                             </xsl:otherwise>
                           </xsl:choose>
                               {
                                   parent_attri = (axiom_attribute_t*)val;
                                   break;
                               }
                       }
                  }

                  if(parent_attri)
                  {
                    attrib_text = axiom_attribute_get_value(parent_attri, env);
                  }
                  else
                  {
                    /* this is hoping that attribute is stored in "<xsl:value-of select="$propertyName"/>", this happnes when name is in default namespace */
                    attrib_text = axiom_element_get_attribute_value_by_name(parent_element, env, "<xsl:value-of select="$propertyName"/>");
                  }

                  if(attrib_text != NULL)
                  {
                      <!-- here only simple type possible -->
                      <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atoi(attrib_text));
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (char)(*attrib_text));
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atoi(attrib_text));
                        </xsl:when>

                        <!-- add int64_t s -->
                        <xsl:when test="$nativePropertyType='int64_t'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, axutil_strtol(attrib_text, (char**)NULL, 0));
                        </xsl:when>
                        <xsl:when test="$nativePropertyType='uint64_t'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, axutil_strtoul(attrib_text, (char**)NULL, 0));
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atof(attrib_text));
                        </xsl:when>
                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atof(attrib_text));
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, attrib_text);
                        </xsl:when>


                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                          prefix_found = AXIS2_FALSE;
                          for(cp = attrib_text; *cp; cp ++)
                          {
                              if(*cp == ':')
                              {
                                  *cp = '\0';
                                  cp ++;
                                  prefix_found  = AXIS2_TRUE;
                                  break;
                              }
                          }

                          if(prefix_found)
                          {
                              /* node value contain the prefix */
                              qname_ns = axiom_element_find_namespace_uri((axiom_element_t*)axiom_node_get_data_element(parent, env), env, attrib_text, parent);
                          }
                          else
                          {
                              /* Then it is the default namespace */
                              cp = attrib_text;
                              qname_ns = axiom_element_get_default_namespace((axiom_element_t*)axiom_node_get_data_element(parent, env), env, parent);
                          }

                           <!-- we are done extracting info, just set the extracted value to the qname -->

                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env,
                                                          axutil_qname_create(
                                                                env, 
                                                                cp, /* cp contain the localname */
                                                                axiom_namespace_get_uri(qname_ns, env),
                                                                axiom_namespace_get_prefix(qname_ns, env)));
                        </xsl:when>


                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, axutil_uri_parse_string(env, attrib_text));
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, axutil_duration_create_from_string(env, attrib_text));
                        </xsl:when>
                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           if (!axutil_strcmp(attrib_text, "TRUE") || !axutil_strcmp(attrib_text, "true"))
                           {
                               <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, AXIS2_TRUE);
                           }
                           else
                           {
                               <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, AXIS2_FALSE);
                           }
                        </xsl:when>
                        <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, atoi(attrib_text));
                        </xsl:when>
                        <!-- add date_time_t* s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           element = (void*)axutil_date_time_create(env);
                           axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                      attrib_text);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (<xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <!-- add hex_binary_t* s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           element = (void*)axutil_base64_binary_create(env);
                           axutil_base64_binary_set_encoded_binary((<xsl:value-of select="$nativePropertyType"/>)element), env,
                                                                      attrib_text);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (<xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <xsl:when test="@ours">
                            element =  (void*)<xsl:value-of select="substring-before(@type, '_t*')"/>_create(env);
                            <xsl:value-of select="substring-before(@type, '_t*')"/>_deserialize_from_string((<xsl:value-of select="$nativePropertyType"/>)element, env, attrib_text, parent);
                           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                          env, (<xsl:value-of select="$nativePropertyType"/>)element);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /* can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                        </xsl:otherwise>
                      </xsl:choose>
                    }
                  </xsl:otherwise>
                  </xsl:choose>
                </xsl:when>
                <xsl:when test="$simple"></xsl:when> <!-- just to avoid preceeding code to be parsed in a simple type -->
                <xsl:otherwise> <!-- when it is an element not(@attribute) -->
                  <!-- handles arrays -->
                   <xsl:if test="@isarray">
                    /*
                     * building <xsl:value-of select="$CName"/> array
                     */
                       arr_list = axutil_array_list_create(env, 10);
                   </xsl:if>

                     <!-- for each non attribute properties there will always be an element-->
                     /*
                      * building <xsl:value-of select="$propertyName"/> element
                      */
                     <!-- array and non array build is so different so big choose, when is requried-->
                     <!-- the method of picking the element is depend on the $ordered -->
                     <xsl:choose>
                       <xsl:when test="not(@isarray)">  <!--not an array so continue normal -->
                           <xsl:choose>
                             <xsl:when test="$ordered or not($anon or $istype) or $choice"> <!-- since non-anon and choices has just only one sub element-->
                               <xsl:choose>
                                 <xsl:when test="position()=1">
                                   current_node = first_node;
                                   is_early_node_valid = AXIS2_FALSE;
                                   <!-- Wait until AXIOM_ELEMENT -->
                                   <xsl:if test="not(@any)">
                                    while(current_node &amp;&amp; axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                    {
                                        current_node = axiom_node_get_next_sibling(current_node, env);
                                    }
                                    if(current_node != NULL)
                                    {
                                        current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                        qname = axiom_element_get_qname(current_element, env, current_node);
                                    }
                                   </xsl:if>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    /*
                                     * because elements are ordered this works fine
                                     */
                                  
                                   <!-- current node should contain the ordered value -->
                                   if(current_node != NULL &amp;&amp; is_early_node_valid)
                                   {
                                       current_node = axiom_node_get_next_sibling(current_node, env);
                                       <!-- Wait until AXIOM_ELEMENT -->
                                       <xsl:if test="not(@any)">
                                        while(current_node &amp;&amp; axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                        {
                                            current_node = axiom_node_get_next_sibling(current_node, env);
                                        }
                                        if(current_node != NULL)
                                        {
                                            current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                            qname = axiom_element_get_qname(current_element, env, current_node);
                                        }
                                       </xsl:if>
                                   }
                                   is_early_node_valid = AXIS2_FALSE;
                                 </xsl:otherwise>
                               </xsl:choose> <!-- close for position -1 -->

                               <xsl:choose>
                                 <xsl:when test="@any"></xsl:when>
                                 <xsl:when test="@nsuri and @nsuri != ''">
                                 element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                 </xsl:when>
                                 <xsl:otherwise>
                                 element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                 </xsl:otherwise>
                               </xsl:choose>

                             </xsl:when>
                             <xsl:otherwise> <!-- otherwise for ($ordered), -->
                               /*
                                * because elements are not ordered we should surf all the sibling to pick the right one
                                */
                               for (current_node = first_node; current_node != NULL;
                                             current_node = axiom_node_get_next_sibling(current_node, env))
                               {
                                  if(axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                  {
                                     continue;
                                  }
                                  
                                  current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                  qname = axiom_element_get_qname(current_element, env, current_node);
                                <xsl:choose>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                  element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                  </xsl:when>
                                  <xsl:otherwise>
                                  element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                                  if (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)
                                  {
                                       /* found the requried element */
                                       break;
                                  }
                               }
                             </xsl:otherwise> <!-- close for $ordered or not($anon or $istype) -->
                           </xsl:choose>

                           if (<xsl:if test="@ours"><xsl:value-of select="substring-before(@type, '_t*')"/>_is_particle() || </xsl:if> <!-- is particle test should be done here -->
                                (current_node <xsl:if test="not(@any)">  &amp;&amp; current_element &amp;&amp; (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)</xsl:if>))
                           {
                              if( current_node <xsl:if test="not(@any)">  &amp;&amp; current_element &amp;&amp; (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)</xsl:if>)
                              {
                                is_early_node_valid = AXIS2_TRUE;
                              }
                              <!-- changes to following choose tag should be changed in another 2 places -->
                                 <xsl:choose>
                                    <xsl:when test="@ours">
                                      element = (void*)axis2_extension_mapper_create_from_node(env, &amp;current_node, "<xsl:value-of select="substring-before(@type, '_t*')"/>");

                                      status =  <xsl:value-of select="substring-before(@type, '_t*')"/>_deserialize((<xsl:value-of select="$nativePropertyType"/>)element,
                                                                            env, &amp;current_node, &amp;is_early_node_valid, <xsl:choose><xsl:when test="$choice">AXIS2_TRUE</xsl:when><xsl:otherwise>AXIS2_FALSE</xsl:otherwise></xsl:choose>);
                                      if(AXIS2_FAILURE == status)
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building adb object for element <xsl:value-of select="$propertyName"/>");
                                      }
                                      else
                                      {
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   (<xsl:value-of select="$nativePropertyType"/>)element);
                                      }
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                               text_value);
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                            /*
                                             * axis2_qname_t *qname = NULL;
                                             * axiom_attribute_t *the_attri = NULL;
                                             * 
                                             * qname = axutil_qname_create(env, "nil", "http://www.w3.org/2001/XMLSchema-instance", "xsi");
                                             * the_attri = axiom_element_get_attribute(current_element, env, qname);
                                             */
                                            /* currently thereis a bug in the axiom_element_get_attribute, so we have to go to this bad method */

                                            axiom_attribute_t *the_attri = NULL;
                                            axis2_char_t *attrib_text = NULL;
                                            axutil_hash_t *attribute_hash = NULL;

                                            attribute_hash = axiom_element_get_all_attributes(current_element, env);

                                            attrib_text = NULL;
                                            if(attribute_hash)
                                            {
                                                 axutil_hash_index_t *hi;
                                                 void *val;
                                                 const void *key;
                                        
                                                 for (hi = axutil_hash_first(attribute_hash, env); hi; hi = axutil_hash_next(env, hi)) 
                                                 {
                                                     axutil_hash_this(hi, &amp;key, NULL, &amp;val);
                                                     
                                                     if(strstr((axis2_char_t*)key, "nil|http://www.w3.org/2001/XMLSchema-instance"))
                                                     {
                                                         the_attri = (axiom_attribute_t*)val;
                                                         break;
                                                     }
                                                 }
                                            }

                                            if(the_attri)
                                            {
                                                attrib_text = axiom_attribute_get_value(the_attri, env);
                                            }
                                            else
                                            {
                                                /* this is hoping that attribute is stored in "http://www.w3.org/2001/XMLSchema-instance", this happnes when name is in default namespace */
                                                attrib_text = axiom_element_get_attribute_value_by_name(current_element, env, "nil");
                                            }

                                            if(attrib_text &amp;&amp; 0 == axutil_strcmp(attrib_text, "1"))
                                            {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                            }
                                            else
                                            {
                                                /* after all, we found this is a empty string */
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   "");
                                            }
                                      }
                                      </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                             axutil_uri_parse_string(env, text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                             axutil_duration_create_from_string(env, text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            prefix_found = AXIS2_FALSE;
                                            for(cp = (axis2_char_t*)text_value; *cp; cp ++)
                                            {
                                                if(*cp == ':')
                                                {
                                                    *cp = '\0';
                                                    cp ++;
                                                    prefix_found  = AXIS2_TRUE;
                                                    break;
                                                }
                                            }
                                          
                                            if(prefix_found)
                                            {
                                                /* node value contain  the prefix */
                                                qname_ns = axiom_element_find_namespace_uri(current_element, env, text_value, current_node);
                                            }
                                            else
                                            {
                                                /* Then it is the default namespace */
                                                cp = (axis2_char_t*)text_value;
                                                qname_ns = axiom_element_get_default_namespace(current_element, env, current_node);
                                            }
                                          
                                            <!-- we are done extracting info, just set the extracted value to the qname -->
                                           
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                    env,
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                                       }
                                       <xsl:if test="not(@nillable)">
                                         else
                                         {
                                             AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                             status = AXIS2_FAILURE;
                                         }
                                       </xsl:if>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                             (char)(*text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atoi(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 
                                   </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atoi(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 

                                   </xsl:when>
                                    <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atoi(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if> 

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='float'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atof(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                     </xsl:when>
                                    <xsl:when test="$nativePropertyType='double'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   atof(text_value));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='int64_t'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   axutil_strtol(text_value, (char**)NULL, 0));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='uint64_t'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   axutil_strtoul(text_value, (char**)NULL, 0));
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                      text_value = NULL; /* just to avoid warning */
                                      <xsl:choose>
                                        <xsl:when test="@any">
                                        {
                                          axiom_node_t *current_property_node = current_node;
                                          current_node = axiom_node_get_next_sibling(current_node, env);
                                          axiom_node_detach(current_property_node, env);
                                          status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                          current_property_node);
                                        }
                                        </xsl:when>
                                        <xsl:otherwise>
                                          if(axiom_node_get_first_child(current_node, env))
                                          {
                                              axiom_node_t *current_property_node = axiom_node_get_first_child(current_node, env);
                                              axiom_node_detach(current_property_node, env);
                                              status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                          current_property_node);
                                          }
                                          else
                                          {
                                              status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                          NULL);
                                          }
                                        </xsl:otherwise>
                                      </xsl:choose>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                            if (!axutil_strcasecmp(text_value , "true"))
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                 AXIS2_TRUE);
                                            }
                                            else
                                            {
                                                status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                      AXIS2_FALSE);
                                            }
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>
                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          element = (void*)axutil_date_time_create(env);
                                          status = axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                          text_value);
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              if(element != NULL)
                                              {
                                                  axutil_date_time_free((axutil_date_time_t*)element, env);
                                              }
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                          }
                                          else
                                          {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                       (<xsl:value-of select="$nativePropertyType"/>)element);
                                          }
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                    </xsl:when>
                                    <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                      text_value = axiom_element_get_text(current_element, env, current_node);
                                      if(text_value != NULL)
                                      {
                                          element = (void*)axutil_base64_binary_create(env);
                                          status = axutil_base64_binary_set_encoded_binary((axutil_base64_binary_t*)element, env,
                                                                          text_value);
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              if(element != NULL)
                                              {
                                                 axutil_base64_binary_free((axutil_base64_binary_t*)element, env);
                                              }
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                          }
                                          else
                                          {
                                            status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                       (<xsl:value-of select="$nativePropertyType"/>)element);
                                          }
                                      }
                                      <xsl:if test="not(@nillable)">
                                      else
                                      {
                                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                          status = AXIS2_FAILURE;
                                      }
                                      </xsl:if>

                                    </xsl:when>
                                    <xsl:otherwise>
                                      <!-- TODO: add other types here -->
                                      /* Imposible to handle the request type - so please do it manually */
                                      text_value = NULL;
                                    </xsl:otherwise>
                                 </xsl:choose>
                                 if(AXIS2_FAILURE ==  status)
                                 {
                                     AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> ");
                                     if(element_qname)
                                     {
                                         axutil_qname_free(element_qname, env);
                                     }
                                     return AXIS2_FAILURE;
                                 }
                              }
                           <xsl:if test="not(@nillable) and not(@minOccurs=0) and not($choice)">
                              else if(!dont_care_minoccurs)
                              {
                                  if(element_qname)
                                  {
                                      axutil_qname_free(element_qname, env);
                                  }
                                  /* this is not a nillable element*/
                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "non nillable or minOuccrs != 0 element <xsl:value-of select="$propertyName"/> missing");
                                  return AXIS2_FAILURE;
                              }
                           </xsl:if>
                        </xsl:when>
                        <xsl:otherwise> <!-- when it is all the way an array -->
                           <xsl:if test="@any">
                            /* 'any' arrays are not handling correctly when there are other elements mixed with the 'any' element. */
                           </xsl:if>
                           <xsl:choose>
                             <xsl:when test="$ordered or not($anon or $istype) or $choice"> <!-- all the elements should follow this -->
                                <xsl:choose>
                                  <xsl:when test="@any"></xsl:when>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                  </xsl:when>
                                  <xsl:otherwise>
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                               
                               for (i = 0, sequence_broken = 0, current_node = <xsl:choose>
                                             <xsl:when test="position()=1">first_node</xsl:when>
                                             <xsl:otherwise>(is_early_node_valid?axiom_node_get_next_sibling(current_node, env):current_node)</xsl:otherwise></xsl:choose>; !sequence_broken &amp;&amp; current_node != NULL;) 
                                             <!-- We are not moving current_node to next sibling here if it an any type, because we already have done the move -->
                               {
                                  if(axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                  {
                                     current_node =axiom_node_get_next_sibling(current_node, env);
                                     is_early_node_valid = AXIS2_FALSE;
                                     continue;
                                  }
                                  <xsl:if test="not(@any)">
                                  current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                  qname = axiom_element_get_qname(current_element, env, current_node);

                                  if (<xsl:if test="@ours"><xsl:value-of select="substring-before(@type, '_t*')"/>_is_particle() || </xsl:if> <!-- is particle test should be done here -->
                                    (current_node &amp;&amp; current_element &amp;&amp; (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)))
                                  {
                                  </xsl:if>
                                      if( current_node &amp;&amp; current_element &amp;&amp; (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>))
                                      {
                                          is_early_node_valid = AXIS2_TRUE;
                                      }
                                      <!-- changes to following choose tag should be changed in another 2 places -->
                                     <xsl:choose>
                                        <xsl:when test="@ours">
                                          element = (void*)axis2_extension_mapper_create_from_node(env, &amp;current_node, "<xsl:value-of select="substring-before(@type, '_t*')"/>");
                                          
                                          status =  <xsl:value-of select="substring-before(@type, '_t*')"/>_deserialize((<xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                                 &amp;current_node, &amp;is_early_node_valid, <xsl:choose><xsl:when test="$choice">AXIS2_TRUE</xsl:when><xsl:otherwise>AXIS2_FALSE</xsl:otherwise></xsl:choose>);
                                          
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                          }
                                          else
                                          {
                                            axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              axutil_array_list_add_at(arr_list, env, i, axutil_strdup(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                              /*
                                               * axis2_qname_t *qname = NULL;
                                               * axiom_attribute_t *the_attri = NULL;
                                               * 
                                               * qname = axutil_qname_create(env, "nil", "http://www.w3.org/2001/XMLSchema-instance", "xsi");
                                               * the_attri = axiom_element_get_attribute(current_element, env, qname);
                                               */
                                           
                                              /* currently thereis a bug in the axiom_element_get_attribute, so we have to go to this bad method */
                                             
                                              axiom_attribute_t *the_attri = NULL;
                                              axis2_char_t *attrib_text = NULL;
                                              axutil_hash_t *attribute_hash = NULL;
                                             
                                              attribute_hash = axiom_element_get_all_attributes(current_element, env);
                                             
                                              attrib_text = NULL;
                                              if(attribute_hash)
                                              {
                                                   axutil_hash_index_t *hi;
                                                   void *val;
                                                   const void *key;
                                             
                                                   for (hi = axutil_hash_first(attribute_hash, env); hi; hi = axutil_hash_next(env, hi)) 
                                                   {
                                                       axutil_hash_this(hi, &amp;key, NULL, &amp;val);
                                                       
                                                       if(strstr((axis2_char_t*)key, "nil|http://www.w3.org/2001/XMLSchema-instance"))
                                                       {
                                                           the_attri = (axiom_attribute_t*)val;
                                                           break;
                                                       }
                                                   }
                                              }
                                             
                                              if(the_attri)
                                              {
                                                  attrib_text = axiom_attribute_get_value(the_attri, env);
                                              }
                                              else
                                              {
                                                  /* this is hoping that attribute is stored in "http://www.w3.org/2001/XMLSchema-instance", this happnes when name is in default namespace */
                                                  attrib_text = axiom_element_get_attribute_value_by_name(current_element, env, "nil");
                                              }
                                             
                                              if(attrib_text &amp;&amp; 0 == axutil_strcmp(attrib_text, "1"))
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                  status = AXIS2_FAILURE;
                                              }
                                              else
                                              {
                                                  /* after all, we found this is a empty string */
                                                  axutil_array_list_add_at(arr_list, env, i, axutil_strdup(env, ""));
                                              }
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              prefix_found = AXIS2_FALSE;
                                              for(cp = (axis2_char_t*)text_value; *cp; cp ++)
                                              {
                                                  if(*cp == ':')
                                                  {
                                                      *cp = '\0';
                                                      cp ++;
                                                      prefix_found  = AXIS2_TRUE;
                                                      break;
                                                  }
                                              }
                                              
                                              if(prefix_found)
                                              {
                                                  /* node value contain  the prefix */
                                                  qname_ns = axiom_element_find_namespace_uri(current_element, env, text_value, current_node);
                                              }
                                              else
                                              {
                                                  /* Then it is the default namespace */
                                                  cp = (axis2_char_t*)text_value;
                                                  qname_ns = axiom_element_get_default_namespace(current_element, env, current_node);
                                              }
                                              
                                              <!-- we are done extracting info, just set the extracted value to the qname -->
                                              
                                              axutil_array_list_add_at(arr_list, env, i, (void*)
                                                      axutil_qname_create(
                                                            env, 
                                                            cp, /* cp contain the localname */
                                                            axiom_namespace_get_uri(qname_ns, env),
                                                            axiom_namespace_get_prefix(qname_ns, env)));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              axutil_array_list_add_at(arr_list, env, i, (void*)axutil_uri_parse_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, (void*)axutil_duration_create_from_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               /* we keeps ints in arrays from their pointers */
                                               element = AXIS2_MALLOC(env-> allocator, 64);
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = (char)(*text_value);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(int));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(axis2_byte_t));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(short));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='float'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(float));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='double'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               /* we keeps float in arrays from their pointers */
                                               element = AXIS2_MALLOC(env-> allocator, sizeof(double));
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int64_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps int64_t in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(int64_t));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = axutil_strtol(text_value, (char**)NULL, 0);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='uint64_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps int64_t in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(uint64_t));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = axutil_strtoul(text_value, (char**)NULL, 0);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                          text_value = NULL; /* just to avoid warning */
                                          <xsl:choose>
                                            <xsl:when test="@any">
                                            {
                                              axiom_node_t *current_property_node = current_node;
                                              current_node = axiom_node_get_next_sibling(current_node, env);
                                              axiom_node_detach(current_property_node, env);
                                              axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                            }
                                            </xsl:when>
                                            <xsl:otherwise>
                                              if(axiom_node_get_first_child(current_node, env))
                                              {
                                                  axiom_node_t *current_property_node = axiom_node_get_first_child(current_node, env);
                                                  axiom_node_detach(current_property_node, env);
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                              }
                                              else
                                              {
                                                  status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                              NULL);
                                              }
                                            </xsl:otherwise>
                                          </xsl:choose>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               if (!axutil_strcasecmp (text_value , "true"))
                                               {
                                                  element = AXIS2_MALLOC(env->allocator,sizeof(axis2_bool_t));
                                                  (*(<xsl:value-of select="$nativePropertyType"/>*)element) = AXIS2_TRUE;
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)element);
                                               }
                                               else
                                               {
                                                  element = AXIS2_MALLOC(env->allocator,sizeof(axis2_bool_t));
                                                  (*(<xsl:value-of select="$nativePropertyType"/>*)element) = AXIS2_FALSE;
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)element);

                                               }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = (void*)axutil_date_time_create(env);
                                              status = axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                      " %d :: %s", env->error->error_number,
                                                                      AXIS2_ERROR_GET_MESSAGE(env->error));
                                              }
                                              else
                                              {
                                                axutil_array_list_add_at(arr_list, env, i, element);
                                              }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = (void*)axutil_base64_binary_create(env);
                                              status = axutil_base64_binary_set_encoded_binary((axutil_base64_binary_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                      " %d :: %s", env->error->error_number,
                                                                      AXIS2_ERROR_GET_MESSAGE(env->error));
                                              }
                                              else
                                              {
                                                axutil_array_list_add_at(arr_list, env, i, element);
                                              }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:otherwise>
                                          <!-- TODO: add other types here -->
                                          /* imposible to handle the request type - so please do it manually */
                                          text_value = NULL;
                                        </xsl:otherwise>
                                     </xsl:choose>
                                     if(AXIS2_FAILURE ==  status)
                                     {
                                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> ");
                                         if(element_qname)
                                         {
                                            axutil_qname_free(element_qname, env);
                                         }
                                         if(arr_list)
                                         {
                                            axutil_array_list_free(arr_list, env);
                                         }
                                         return AXIS2_FAILURE;
                                     }

                                     i ++;
                                    <xsl:if test="not(@any)">current_node = axiom_node_get_next_sibling(current_node, env);</xsl:if>

                                 <xsl:if test="not(@any)">
                                  }
                                  else
                                  {
                                      is_early_node_valid = AXIS2_FALSE;
                                      sequence_broken = 1;
                                  }
                                  </xsl:if>
                               }

                               <xsl:if test="not(@any)">
                                   if (i &lt; <xsl:value-of select="@minOccurs"/>)
                                   {
                                     /* found element out of order */
                                     AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> (@minOccurs = '<xsl:value-of select="@minOccurs"/>') only have %d elements", i);
                                     if(element_qname)
                                     {
                                        axutil_qname_free(element_qname, env);
                                     }
                                     if(arr_list)
                                     {
                                        axutil_array_list_free(arr_list, env);
                                     }
                                     return AXIS2_FAILURE;
                                   }
                               </xsl:if>

                               if(0 == axutil_array_list_size(arr_list,env))
                               {
                                    axutil_array_list_free(arr_list, env);
                               }
                               else
                               {
                                    status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   arr_list);
                               }

                             </xsl:when>
                             <xsl:otherwise> <!-- otherwse for "$ordered or not($anon or $istype)" -->
                                <xsl:choose>
                                  <xsl:when test="@any"></xsl:when>
                                  <xsl:when test="@nsuri and @nsuri != ''">
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", "<xsl:value-of select="@nsuri"/>", NULL);
                                  </xsl:when>
                                  <xsl:otherwise>
                                    element_qname = axutil_qname_create(env, "<xsl:value-of select="$propertyName"/>", NULL, NULL);
                                  </xsl:otherwise>
                                </xsl:choose>
                                /*
                                 * because elements are not ordered we should surf all the sibling to pick the right one
                                 */
                               <!-- For non-ordered arrays we are not using is_early_node_valid? -->
                               for (i = 0, current_node = first_node; current_node != NULL; <xsl:if test="not(@any)">current_node = axiom_node_get_next_sibling(current_node, env)</xsl:if>)
                                             <!-- We are not moving current_node to next sibling here if it an any type, because we already have done the move -->
                               {
                                  if(axiom_node_get_node_type(current_node, env) != AXIOM_ELEMENT)
                                  {
                                     <xsl:if test="@any">
                                     current_node = axiom_node_get_next_sibling(current_node, env);
                                     </xsl:if>
                                     continue;
                                  }
                                  <xsl:if test="not(@any)">
                                  current_element = (axiom_element_t *)axiom_node_get_data_element(current_node, env);
                                  qname = axiom_element_get_qname(current_element, env, current_node);

                                  if (axutil_qname_equals(element_qname, env, qname)<xsl:if test="not(@nsuri) or @nsuri=''"> || !axutil_strcmp("<xsl:value-of select="$propertyName"/>", axiom_element_get_localname(current_element, env))</xsl:if>)
                                  {
                                  </xsl:if>
                                       /* found the requried element */
                                       is_early_node_valid = AXIS2_TRUE;
                                      <!-- changes to following choose tag should be changed in another 2 places -->
                                     <xsl:choose>
                                        <xsl:when test="@ours">
                                          element = (void*)axis2_extension_mapper_create_from_node(env, &amp;current_node, "<xsl:value-of select="substring-before(@type, '_t*')"/>");
                                          
                                          status =  <xsl:value-of select="substring-before(@type, '_t*')"/>_deserialize((<xsl:value-of select="$nativePropertyType"/>)element, env,
                                                                                 &amp;current_node, &amp;is_early_node_valid, <xsl:choose><xsl:when test="$choice">AXIS2_TRUE</xsl:when><xsl:otherwise>AXIS2_FALSE</xsl:otherwise></xsl:choose>);
                                          if(AXIS2_FAILURE ==  status)
                                          {
                                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> "
                                                                  " %d :: %s", env->error->error_number,
                                                                  AXIS2_ERROR_GET_MESSAGE(env->error));
                                          }
                                          else
                                          {
                                            axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, axutil_strdup(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                             /*
                                               * axis2_qname_t *qname = NULL;
                                               * axiom_attribute_t *the_attri = NULL;
                                               * 
                                               * qname = axutil_qname_create(env, "nil", "http://www.w3.org/2001/XMLSchema-instance", "xsi");
                                               * the_attri = axiom_element_get_attribute(current_element, env, qname);
                                               */
                                           
                                              /* currently thereis a bug in the axiom_element_get_attribute, so we have to go to this bad method */
                                             
                                              axiom_attribute_t *the_attri = NULL;
                                              axis2_char_t *attrib_text = NULL;
                                              axutil_hash_t *attribute_hash = NULL;
                                             
                                              attribute_hash = axiom_element_get_all_attributes(current_element, env);
                                             
                                              attrib_text = NULL;
                                              if(attribute_hash)
                                              {
                                                   axutil_hash_index_t *hi;
                                                   void *val;
                                                   const void *key;
                                             
                                                   for (hi = axutil_hash_first(attribute_hash, env); hi; hi = axutil_hash_next(env, hi)) 
                                                   {
                                                       axutil_hash_this(hi, &amp;key, NULL, &amp;val);
                                                       
                                                       if(strstr((axis2_char_t*)key, "nil|http://www.w3.org/2001/XMLSchema-instance"))
                                                       {
                                                           the_attri = (axiom_attribute_t*)val;
                                                           break;
                                                       }
                                                   }
                                              }
                                             
                                              if(the_attri)
                                              {
                                                  attrib_text = axiom_attribute_get_value(the_attri, env);
                                              }
                                              else
                                              {
                                                  /* this is hoping that attribute is stored in "http://www.w3.org/2001/XMLSchema-instance", this happnes when name is in default namespace */
                                                  attrib_text = axiom_element_get_attribute_value_by_name(current_element, env, "nil");
                                              }
                                             
                                              if(attrib_text &amp;&amp; 0 == axutil_strcmp(attrib_text, "1"))
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                  status = AXIS2_FAILURE;
                                              }
                                              else
                                              {
                                                  /* after all, we found this is a empty string */
                                                  axutil_array_list_add_at(arr_list, env, i, axutil_strdup(env, ""));
                                              }
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                            prefix_found = AXIS2_FALSE;
                                            for(cp = (axis2_char_t*)text_value; *cp; cp ++)
                                            {
                                                if(*cp == ':')
                                                {
                                                    *cp = '\0';
                                                    cp ++;
                                                    prefix_found  = AXIS2_TRUE;
                                                    break;
                                                }
                                            }
                                          
                                            if(prefix_found)
                                            {
                                                /* node value contain  the prefix */
                                                qname_ns = axiom_element_find_namespace_uri(current_element, env, text_value, current_node);
                                            }
                                            else
                                            {
                                                /* Then it is the default namespace */
                                                cp = (axis2_char_t*)text_value;
                                                qname_ns = axiom_element_get_default_namespace(current_element, env, current_node);
                                            }
                                          
                                            <!-- we are done extracting info, just set the extracted value to the qname -->
                                           
                                            axutil_array_list_add_at(arr_list, env, i, (void*)
                                                    axutil_qname_create(
                                                          env, 
                                                          cp, /* cp contain the localname */
                                                          axiom_namespace_get_uri(qname_ns, env),
                                                          axiom_namespace_get_prefix(qname_ns, env)));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, (void*)axutil_uri_parse_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                                axutil_array_list_add_at(arr_list, env, i, (void*)axutil_duration_create_from_string(env, text_value));
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, 64);
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = (char)(*text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(int));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              /* we keeps ints in arrays from their pointers */
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(int));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               /* we keeps ints in arrays from their pointers */
                                               element = AXIS2_MALLOC(env-> allocator, sizeof(short));
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atoi(text_value);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='float'">
                                          /* we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(float));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='double'">
                                          /* we keeps float in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              element = AXIS2_MALLOC(env-> allocator, sizeof(double));
                                              (*(<xsl:value-of select="$nativePropertyType"/>*)element) = atof(text_value);
                                              axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='int64_t'">
                                          /* we keeps int64_t in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               element = AXIS2_MALLOC(env-> allocator, sizeof(int64_t));
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = axutil_strtol(text_value, (char**)NULL,0);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='uint64_t'">
                                          /* we keeps int64_t in arrays from their pointers */
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                               element = AXIS2_MALLOC(env-> allocator, sizeof(uint64_t));
                                               (*(<xsl:value-of select="$nativePropertyType"/>*)element) = axutil_strtoul(text_value, (char**)NULL, 0);
                                               axutil_array_list_add_at(arr_list, env, i, element);
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                                          text_value = NULL; /* just to avoid warning */
                                          <xsl:choose>
                                            <xsl:when test="@any">
                                            {
                                              axiom_node_t *current_property_node = current_node;
                                              current_node = axiom_node_get_next_sibling(current_node, env);
                                              axiom_node_detach(current_property_node, env);
                                              axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                            }
                                            </xsl:when>
                                            <xsl:otherwise>
                                              if(axiom_node_get_first_child(current_node, env))
                                              {
                                                  axiom_node_t *current_property_node = axiom_node_get_first_child(current_node, env);
                                                  axiom_node_detach(current_property_node, env);
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)current_property_node);
                                              }
                                            </xsl:otherwise>
                                          </xsl:choose>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              if (!axutil_strcasecmp (text_value , "true"))
                                              {
                                                  element = AXIS2_MALLOC(env->allocator,sizeof(axis2_bool_t));
                                                  (*(<xsl:value-of select="$nativePropertyType"/>*)element) = AXIS2_TRUE;
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)element);
                                              }
                                              else
                                              {
                                                  element = AXIS2_MALLOC(env->allocator,sizeof(axis2_bool_t));
                                                  (*(<xsl:value-of select="$nativePropertyType"/>*)element) = AXIS2_FALSE;
                                                  axutil_array_list_add_at(arr_list, env, i, (void*)element);
                                              }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                                          element = (void*)axutil_date_time_create(env);
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              status = axutil_date_time_deserialize_date_time((axutil_date_time_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                              }
                                              else
                                              {
                                                axutil_array_list_add_at(arr_list, env, i, element);
                                              }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>

                                        </xsl:when>
                                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                                          element = (void*)axutil_base64_binary_create(env);
                                          text_value = axiom_element_get_text(current_element, env, current_node);
                                          if(text_value != NULL)
                                          {
                                              status = axutil_base64_binary_set_encoded_binary((axutil_base64_binary_t*)element, env,
                                                                              text_value);
                                              if(AXIS2_FAILURE ==  status)
                                              {
                                                  AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in building element <xsl:value-of select="$propertyName"/> ");
                                              }
                                              else
                                              {
                                                 axutil_array_list_add_at(arr_list, env, i, element);
                                              }
                                          }
                                          <xsl:if test="not(@nillable)">
                                          else
                                          {
                                                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "NULL value is set to a non nillable element <xsl:value-of select="$propertyName"/>");
                                                status = AXIS2_FAILURE;
                                          }
                                          </xsl:if>


                                        </xsl:when>
                                        <xsl:otherwise>
                                          <!-- TODO: add other types here -->
                                          /* imposible to handle the request type - so please do it manually */
                                          text_value = NULL;
                                        </xsl:otherwise>
                                     </xsl:choose>
                                     if(AXIS2_FAILURE ==  status)
                                     {
                                         if(element_qname)
                                         {
                                             axutil_qname_free(element_qname, env);
                                         }
                                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "failed in setting the value for <xsl:value-of select="$propertyName"/> ");
                                         return AXIS2_FAILURE;
                                     }

                                     i ++;
                                  <xsl:if test="not(@any)">
                                  }
                                  </xsl:if>
                               }
                               status = <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env,
                                                                   arr_list);
                             </xsl:otherwise> <!--closing otherwise for "$ordered  or not($anon or $istype)" -->
                           </xsl:choose> <!-- chooses for ordered or not @ordered or not($anon or $istype)-->
                        </xsl:otherwise> <!-- closing when it is all the way an array -->
                      </xsl:choose> <!-- check array or not -->
                   </xsl:otherwise> <!-- closing when it is an element not(@attribute) -->
                 </xsl:choose> <!--- chooosing for element or attribute -->
                 <xsl:if test="not(@simple)">
                  if(element_qname)
                  {
                     axutil_qname_free(element_qname, env);
                     element_qname = NULL;
                  }
                 </xsl:if>
              </xsl:for-each> <!-- closing for each property -->

            <xsl:if test="$particleClass">
                *dp_parent = current_node;
                *dp_is_early_node_valid = is_early_node_valid;
            </xsl:if>
          return status;
       }

          axis2_bool_t AXIS2_CALL
          <xsl:value-of select="$axis2_name"/>_is_particle()
          {
            <xsl:choose>
              <xsl:when test="$particleClass">
                 return AXIS2_TRUE;
              </xsl:when>
              <xsl:otherwise>
                 return AXIS2_FALSE;
              </xsl:otherwise>
            </xsl:choose>
          }


          void AXIS2_CALL
          <xsl:value-of select="$axis2_name"/>_declare_parent_namespaces(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, axiom_element_t *parent_element,
                    axutil_hash_t *namespaces, int *next_ns_index)
          {
            <xsl:variable name="check_anything_to_declare">
                  <xsl:for-each select="property">
                    <xsl:if test="@type='axutil_qname_t*'">yes</xsl:if>
                  </xsl:for-each>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="contains($check_anything_to_declare, 'yes')">
                    axiom_namespace_t *element_ns = NULL;
                    axis2_char_t *qname_uri;
                    axis2_char_t *qname_prefix;
                </xsl:when>
                <xsl:otherwise>
                  /* Here this is an empty function, Nothing to declare */
                </xsl:otherwise>
            </xsl:choose>

                <xsl:for-each select="property">
                  <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="CName"><xsl:value-of select="@cname"/></xsl:variable>
    
                  <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                     <xsl:choose>
                       <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                       <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                  <xsl:choose>
                    <!-- add axutil_qname_t namespaces -->
                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      qname_uri = axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                      if(qname_uri &amp;&amp; !axutil_strcmp(qname_uri, ""))
                      {
                          if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                          {
                              qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                              
                              sprintf(qname_prefix, "q%d", (*next_ns_index)++); <!-- just different prefix for the special case -->
                              axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);

                              if(parent_element)
                              {
                                    element_ns = axiom_namespace_create(env, qname_uri,
                                                                        qname_prefix);
                                    axiom_element_declare_namespace_assume_param_ownership(parent_element, env, element_ns);
                              }
                          }
                      }
                    </xsl:when>
                  </xsl:choose>
              </xsl:for-each> <!--closing the for-each select="property" -->
          }

        <xsl:if test="@simple">
            axis2_char_t* AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_serialize_to_string(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, axutil_hash_t *namespaces)
            {
                axis2_char_t *text_value = NULL;
                axis2_char_t *qname_uri = NULL;
                axis2_char_t *qname_prefix = NULL;
                <xsl:if test="itemtype">
                   int i;
                   int allocated_len = 0;
                   axis2_char_t *tmp_value;
                </xsl:if>

                <xsl:for-each select="property">
                  <xsl:if test="position()=1"> <!-- just to make sure it doesn't go for more than one time -->
                    <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
                    <xsl:variable name="propertyType">
                       <xsl:choose>
                         <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                         <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                         <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                       </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                       <xsl:choose>
                         <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                         <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                       </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="propertyName"><xsl:value-of select="@originalName"/></xsl:variable>
                    <xsl:variable name="CName"><xsl:value-of select="@cname"/></xsl:variable>
      
                    <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                       <xsl:choose>
                         <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                         <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                       </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                       <xsl:choose>
                         <xsl:when test="@isarray">element</xsl:when>
                         <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                       </xsl:choose>
                    </xsl:variable>


                   <xsl:choose>
                      <!-- add int s -->
                      <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>
                      <!-- add axis2_byte_t s -->
                      <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add char s -->
                      <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%c", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add short s -->
                      <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add int64_t s -->
                      <xsl:when test="$nativePropertyType='int64_t'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, AXIS2_PRINTF_INT64_FORMAT_SPECIFIER, (int64_t)<xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <xsl:when test="$nativePropertyType='uint64_t'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, AXIS2_PRINTF_UINT64_FORMAT_SPECIFIER, (uint64_t)<xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add float s -->
                      <xsl:when test="$nativePropertyType='float'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add double s -->
                      <xsl:when test="$nativePropertyType='double'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add axis2_char_t* s -->
                      <xsl:when test="$nativePropertyType='axis2_char_t*'">
                         text_value = (axis2_char_t*)axutil_xml_quote_string(env, <xsl:value-of select="$propertyInstanceName"/>, AXIS2_FALSE);
                         if (!text_value)
                         {
                             text_value = (axis2_char_t*)axutil_strdup(env, <xsl:value-of select="$propertyInstanceName"/>);
                         }
                      </xsl:when>

                      <!-- add axutil_uri_t s -->
                      <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                         text_value = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                      </xsl:when>

                      <!-- add axutil_duration_t s -->
                      <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                         text_value = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                      </xsl:when>

                      <!-- add axutil_qname_t s -->
                      <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                        <!-- namespaces are declared in _declare_parent_namespaces -->
                        qname_uri = axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                        if(qname_uri == NULL)
                        {
                              text_value = (axis2_char_t*)axutil_strdup(env, axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                        }
                        else
                        {
                          qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING);
                          if(qname_prefix != NULL)
                          {
                              text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, 
                                          sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                              axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env)) + 2));
                              sprintf(text_value, "%s:%s", qname_prefix,
                                                        axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                          }
                          else
                          {
                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in serialize_to_string value for <xsl:value-of select="$propertyName"/>, "
                                                          "Prefix is not declared beofre using");
                              return NULL;
                          }
                        }
                      </xsl:when>

                      <!-- add axis2_bool_t s -->
                      <xsl:when test="$nativePropertyType='axis2_bool_t'">
                         <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                         text_value = (axis2_char_t*)(axutil_strdup(env, (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false"));
                      </xsl:when>
                      <!-- add axis2_date_time_t s -->
                      <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                         text_value =  axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                      </xsl:when>
                      <!-- add axis2_base64_binary_t s -->
                      <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                         text_value =  axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                      </xsl:when>
                      <xsl:when test="@ours">
                        if(<xsl:value-of select="$propertyInstanceName"/>)
                        {
                            text_value = <xsl:value-of select="substring-before($nativePropertyType, '_t*')"/>_serialize_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, namespaces);
                        }
                      </xsl:when>
                      <xsl:otherwise>
                        <!--TODO: add new property types -->
                        /* can not handle the property type <xsl:value-of select="$nativePropertyType"/>*/
                        text_value = NULL;
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </xsl:for-each>

            <!-- The section covers the list types, this is a loop always occurs just 1 time-->
            <xsl:for-each select="itemtype">
                <xsl:variable name="propertyType">axutil_array_list_t*</xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
                <xsl:variable name="CName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
 
                <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                   <xsl:choose>
                     <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                     <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                   </xsl:choose>
                </xsl:variable>
                  <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the type stored in the arraylist-->
                     <xsl:choose>
                       <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                       <xsl:when test="@type='unsigned char' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'"><xsl:value-of select="@type"/><xsl:text>*</xsl:text></xsl:when>
                       <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                     </xsl:choose>
                  </xsl:variable>
                
                <xsl:variable name="propertyInstanceName">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:variable>
                <xsl:variable name="justPropertyInstanceName">element</xsl:variable>

                for(i = 0, allocated_len = 2, text_value = (axis2_char_t*) axutil_strdup(env, ""); 
                            i &lt; <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env); i ++)
                {
                  <xsl:value-of select="$nativePropertyType"/> element;
                  axis2_char_t *seperator = (i == <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env) - 1)?"":ADB_DEFAULT_LIST_SEPERATOR;
                  element = <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at(<xsl:value-of select="$name"/>, env, i);
                    
                 <xsl:choose>
                    <!-- add int s -->
                    <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%d%s", text_value, element, seperator);
                    </xsl:when>
                    <!-- add axis2_byte_t s -->
                    <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%d%s", text_value, element, seperator);
                    </xsl:when>

                    <!-- add char s -->
                    <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%c%s", text_value, element, seperator);
                    </xsl:when>

                    <!-- add short s -->
                    <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%d%s", text_value, element, seperator);
                    </xsl:when>

                    <!-- add int64_t s -->
                    <xsl:when test="$nativePropertyType='int64_t'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s" AXIS2_PRINTF_INT64_FORMAT_SPECIFIER "%s", text_value, element, seperator);
                    </xsl:when>

                    <xsl:when test="$nativePropertyType='uint64_t'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s" AXIS2_PRINTF_UINT64_FORMAT_SPECIFIER "%s", text_value, element, seperator);
                    </xsl:when>

                    <!-- add float s -->
                    <xsl:when test="$nativePropertyType='float'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%f%s", text_value, element, seperator);
                    </xsl:when>

                    <!-- add double s -->
                    <xsl:when test="$nativePropertyType='double'">
                       allocated_len += sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%f%s", text_value, element, seperator);
                    </xsl:when>

                    <!-- add axis2_char_t* s -->
                    <xsl:when test="$nativePropertyType='axis2_char_t*'">
                       allocated_len += sizeof (axis2_char_t) * axutil_strlen(element) + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%s%s", text_value, element, seperator);
                    </xsl:when>

                    <!-- add axutil_uri_t s -->
                    <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                       tmp_value = axutil_uri_to_string(element, env, AXIS2_URI_UNP_OMITUSERINFO);
                       allocated_len += sizeof (axis2_char_t) * axutil_strlen(tmp_value) + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%s%s", text_value, tmp_value, seperator);
                    </xsl:when>

                    <!-- add axutil_duration_t s -->
                    <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                       tmp_value = axutil_duration_serialize_duration(element, env);
                       allocated_len += sizeof (axis2_char_t) * axutil_strlen(tmp_value) + 1;
                       text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                       sprintf (text_value, "%s%s%s", text_value, tmp_value, seperator);
                    </xsl:when>

                    <!-- add axutil_qname_t s -->
                    <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      <!-- namespaces are declared in _declare_parent_namespaces -->
                      qname_uri = axutil_qname_get_uri(element, env);
                      if(qname_uri == NULL)
                      {
                           tmp_value = axutil_qname_get_localpart(element, env);
                           allocated_len += sizeof (axis2_char_t) * axutil_strlen(tmp_value) + 1;
                           text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                           sprintf (text_value, "%s%s%s", text_value, tmp_value, seperator);
                      }
                      else
                      {
                        qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING);
                        if(qname_prefix != NULL)
                        {
                            tmp_value = axutil_qname_get_localpart(element, env);
                            allocated_len += sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT  + 1 +
                                                            axutil_strlen(tmp_value) + 2);
                            text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                            
                            sprintf(text_value, "%s%s:%s%s", text_value, qname_prefix,
                                                      tmp_value, seperator);
                        }
                        else
                        {
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in serialize_to_string value for <xsl:value-of select="$propertyName"/>, "
                                                        "Prefix is not declared beofre using");
                            return NULL;
                        }
                     }
                    </xsl:when>

                    <!-- add axis2_bool_t s -->
                    <xsl:when test="$nativePropertyType='axis2_bool_t'">
                       <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                           tmp_value = (axis2_char_t*)((element)?"true":"false");
                           allocated_len += sizeof (axis2_char_t) * axutil_strlen(tmp_value) + 1;
                           text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                           sprintf (text_value, "%s%s%s", text_value, tmp_value, seperator);
                    </xsl:when>
                    <!-- add axis2_date_time_t s -->
                    <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           tmp_value = axutil_date_time_serialize_date_time(element, env);
                           allocated_len += sizeof (axis2_char_t) * axutil_strlen(tmp_value) + 1;
                           text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                           sprintf (text_value, "%s%s%s", text_value, tmp_value, seperator);
                    </xsl:when>
                    <!-- add axis2_base64_binary_t s -->
                    <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           tmp_value = axutil_base64_binary_get_encoded_binary(element, env);
                           allocated_len += sizeof (axis2_char_t) * axutil_strlen(tmp_value) + 1;
                           text_value = (axis2_char_t*) AXIS2_REALLOC (env-> allocator, text_value, allocated_len);
                           sprintf (text_value, "%s%s%s", text_value, tmp_value, seperator);
                    </xsl:when>
                    <xsl:when test="@ours">
                        if(element)
                        {
                            text_value = <xsl:value-of select="substring-before($nativePropertyType, '_t*')"/>_serialize_to_string(element, env, namespaces);
                        }
                    </xsl:when>
                    <xsl:otherwise>
                      <!--TODO: add new property types -->
                      /* can not handle the property type <xsl:value-of select="$nativePropertyType"/>*/
                    </xsl:otherwise>
                  </xsl:choose>
                }
                </xsl:for-each>
                
                <xsl:if test="$isUnion">
                
                void *element = NULL;
                if(!<xsl:value-of select="$name"/>->current_value || !axutil_strcmp("",<xsl:value-of select="$name"/>->current_value))
                {
                    text_value = NULL;
                }
                <xsl:for-each select="memberType">
                   <xsl:variable name="member_type" select="@type"/>
                   <xsl:variable name="member_name"><xsl:text></xsl:text><xsl:value-of select="@originalName"/></xsl:variable>   
                   <xsl:variable name="propertyInstanceName"><xsl:value-of select="$name"/>->member_type._<xsl:value-of select="$member_name"/></xsl:variable>   
                  else if(!axutil_strcmp("<xsl:value-of select="@originalName"/>",<xsl:value-of select="$name"/>->current_value))
                  {
                   <xsl:choose>
                      <!-- add int s -->
                      <xsl:when test="$member_type='int' or $member_type='unsigned int'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>
                      <!-- add axis2_byte_t s -->
                      <xsl:when test="$member_type='axis2_byte_t' or $member_type='axis2_unsigned_byte_t'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add char s -->
                      <xsl:when test="$member_type='char' or $member_type='unsigned char'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%c", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add short s -->
                      <xsl:when test="$member_type='short' or $member_type='unsigned short'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add int64_t s -->
                      <xsl:when test="$member_type='int64_t'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, AXIS2_PRINTF_INT64_FORMAT_SPECIFIER, (int64_t)<xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <xsl:when test="$member_type='uint64_t'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, AXIS2_PRINTF_UINT64_FORMAT_SPECIFIER, (uint64_t)<xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add float s -->
                      <xsl:when test="$member_type='float'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add double s -->
                      <xsl:when test="$member_type='double'">
                         text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                         sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add axis2_char_t* s -->
                      <xsl:when test="$member_type='axis2_char_t*'">
                         text_value = (axis2_char_t*)axutil_strdup(env, <xsl:value-of select="$propertyInstanceName"/>);
                      </xsl:when>

                      <!-- add axutil_uri_t s -->
                      <xsl:when test="$member_type='axutil_uri_t*'">
                         text_value = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                      </xsl:when>

                      <!-- add axutil_duration_t s -->
                      <xsl:when test="$member_type='axutil_duration_t*'">
                         text_value = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                      </xsl:when>

                      <!-- add axutil_qname_t s -->
                      <xsl:when test="$member_type='axutil_qname_t*'">
                        <!-- namespaces are declared in _declare_parent_namespaces -->
                        qname_uri = axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                        if(qname_uri == NULL)
                        {
                              text_value = (axis2_char_t*)axutil_strdup(env, axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                        }
                        else
                        {
                          qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING);
                          if(qname_prefix != NULL)
                          {
                              text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, 
                                          sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                              axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env)) + 2));
                              sprintf(text_value, "%s:%s", qname_prefix,
                                                        axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                          }
                          else
                          {
                              AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in serialize_to_string value for <xsl:value-of select="$member_name"/>, "
                                                          "Prefix is not declared beofre using");
                              return NULL;
                          }
                        }
                      </xsl:when>

                      <!-- add axis2_bool_t s -->
                      <xsl:when test="$member_type='axis2_bool_t'">
                         <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                         text_value = (axis2_char_t*)(axutil_strdup(env, (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false"));
                      </xsl:when>
                      <!-- add axis2_date_time_t s -->
                      <xsl:when test="$member_type='axutil_date_time_t*'">
                         text_value =  axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                      </xsl:when>
                      <!-- add axis2_base64_binary_t s -->
                      <xsl:when test="$member_type='axutil_base64_binary_t*'">
                         text_value =  axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                      </xsl:when>
                      <xsl:when test="@ours">
                        if(<xsl:value-of select="$propertyInstanceName"/>)
                        {
                            text_value = <xsl:value-of select="substring-before($member_type, '_t*')"/>_serialize_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, namespaces);
                        }
                      </xsl:when>
                      <xsl:otherwise>
                        <!--TODO: add new property types -->
                        /* can not handle the property type <xsl:value-of select="$member_type"/>*/
                        text_value = NULL;
                      </xsl:otherwise>
                    </xsl:choose>
                }
                </xsl:for-each>
                </xsl:if>
                return text_value;
            }
        </xsl:if>
        
        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_serialize(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, axiom_node_t *parent, axiom_element_t *parent_element, int parent_tag_closed, axutil_hash_t *namespaces, int *next_ns_index)
        {
            <!-- Only use the extension mapper for actual types -->
            <xsl:choose>
            <xsl:when test="@type = 'yes'">
            if (<xsl:value-of select="$name"/> == NULL)
            {
                return <xsl:value-of select="$axis2_name"/>_serialize_obj(
                    <xsl:value-of select="$name"/>, env, parent, parent_element, parent_tag_closed, namespaces, next_ns_index);
            }
            else
            {
                return axis2_extension_mapper_serialize(
                    (adb_type_t*) <xsl:value-of select="$name"/>, env, parent, parent_element, parent_tag_closed, namespaces, next_ns_index, "<xsl:value-of select="$axis2_name"/>");
            }
            </xsl:when>
            <xsl:otherwise>
                return <xsl:value-of select="$axis2_name"/>_serialize_obj(
                    <xsl:value-of select="$name"/>, env, parent, parent_element, parent_tag_closed, namespaces, next_ns_index);
            </xsl:otherwise>
            </xsl:choose>
        }

        axiom_node_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_serialize_obj(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, axiom_node_t *parent, axiom_element_t *parent_element, int parent_tag_closed, axutil_hash_t *namespaces, int *next_ns_index)
        {
            <!-- first declaration part -->
            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
               axiom_attribute_t *text_attri = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:if test="@type or property/@attribute">
             axis2_char_t *string_to_stream;
            </xsl:if>
         
         axiom_node_t* current_node = NULL;
         int tag_closed = 0;
         <xsl:if test="@type or $isUnion">
         axis2_char_t* xsi_prefix = NULL;
         </xsl:if>
         <xsl:if test="@type">
         axis2_char_t* type_attrib = NULL;
         axiom_namespace_t* xsi_ns = NULL;
         axiom_attribute_t* xsi_type_attri = NULL;
         </xsl:if>
         <!--now distinguise the properties specific to simple types -->
         <xsl:choose>
           <xsl:when test="@simple">
            axiom_data_source_t *data_source = NULL;
            axutil_stream_t *stream = NULL;
            axis2_char_t *text_value;
             <xsl:for-each select="property/@attribute">
              <xsl:if test="position()=1">
               axiom_namespace_t *ns1 = NULL;
               axis2_char_t *p_prefix = NULL;
              </xsl:if>
             </xsl:for-each>
           </xsl:when>

           <!-- non simple types -->
           <xsl:otherwise>
                axiom_namespace_t *ns1 = NULL;

                axis2_char_t *qname_uri = NULL;
                axis2_char_t *qname_prefix = NULL;
                axis2_char_t *p_prefix = NULL;
                axis2_bool_t ns_already_defined;
            <xsl:for-each select="property/@isarray">
             <xsl:if test="position()=1">
               int i = 0;
               int count = 0;
               void *element = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="property">
                <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
                <xsl:choose>
                    <xsl:when test="not(@type) or (@ours='yes' and (@type='uri' or @type='qname' or @type='date_time' or @type='base64_binary' or @type='char')) or @type='char' or @type='axis2_char_t*' or @type='axutil_base64_binary_t*' or @type='axutil_date_time_t*' or @type='axiom_node_t*' or @type='axutil_duration_t*' or @type='axutil_uri_t*' or @type='axutil_qname_t*'">
                    axis2_char_t *text_value_<xsl:value-of select="$position"/>;
                    axis2_char_t *text_value_<xsl:value-of select="$position"/>_temp;
                    </xsl:when>
                    <xsl:otherwise>
                    axis2_char_t text_value_<xsl:value-of select="$position"/>[ADB_DEFAULT_DIGIT_LIMIT];
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

            <xsl:for-each select="property/@attribute">
             <xsl:if test="position()=1">
                axis2_char_t *text_value = NULL;
             </xsl:if>
            </xsl:for-each>
            <xsl:if test="property and (not(property/@attribute) or property/@attribute='' or property/@notattribute)">
               axis2_char_t *start_input_str = NULL;
               axis2_char_t *end_input_str = NULL;
               unsigned int start_input_str_len = 0;
               unsigned int end_input_str_len = 0;
            </xsl:if>
            <!-- Following is in special situatioin where no properties exist -->
               axiom_data_source_t *data_source = NULL;
               axutil_stream_t *stream = NULL;

            <xsl:if test="not(@type)"> <!-- So this is the root of the serialization call tree -->
                int next_ns_index_value = 0;
            </xsl:if>

            AXIS2_ENV_CHECK(env, NULL);
            AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
            
            <xsl:if test="not(@type)"> <!-- So this is the root of the serialization call tree -->
                    namespaces = axutil_hash_make(env);
                    next_ns_index = &amp;next_ns_index_value;
                    <xsl:choose>
                       <xsl:when test="$nsuri and $nsuri != ''"> 
                           ns1 = axiom_namespace_create (env,
                                             "<xsl:value-of select="$nsuri"/>",
                                             "n"); <!-- we are usinig "" instead of <xsl:value-of select="@child-nsuri"/>  -->
                           axutil_hash_set(namespaces, "<xsl:value-of select="$nsuri"/>", AXIS2_HASH_KEY_STRING, axutil_strdup(env, "n"));
                       </xsl:when>
                       <xsl:otherwise> 
                           ns1 = NULL; 
                       </xsl:otherwise>
                    </xsl:choose>
                    <!-- if not(@type) then no doubt the parent is NULL --> 
                    parent_element = axiom_element_create (env, NULL, "<xsl:value-of select="$originalName"/>", ns1 , &amp;parent);
                    
                    <!-- axiom_element_declare_default_namespace(parent_element, env, "<xsl:value-of select="$nsuri"/>"); -->
                    axiom_element_set_namespace(parent_element, env, ns1, parent);


            </xsl:if>
            </xsl:otherwise> <!--otherwise for @simple -->
            </xsl:choose>

                <xsl:if test="@type or @simple">
                    current_node = parent;
                    data_source = (axiom_data_source_t *)axiom_node_get_data_element(current_node, env);
                    if (!data_source)
                        return NULL;
                    stream = axiom_data_source_get_stream(data_source, env); /* assume parent is of type data source */
                    if (!stream)
                        return NULL;
                  </xsl:if>
                <xsl:if test="count(property)!=0">
                  <xsl:if test="not(@type) and not(@simple)">
                    data_source = axiom_data_source_create(env, parent, &amp;current_node);
                    stream = axiom_data_source_get_stream(data_source, env);
                  </xsl:if>
                </xsl:if>


            
            <!--first write attributes tothe parent-->
            <xsl:if test="count(property[@attribute])!=0 or @type">
            if(!parent_tag_closed)
            {
            </xsl:if>
            <xsl:for-each select="property">
              <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@originalName"/></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="namespacePrefix">
                <xsl:choose>
                    <xsl:when test="$nsprefix"><xsl:value-of select="$nsprefix"/><xsl:text>:</xsl:text></xsl:when>
                    <xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="qualifiedPropertyName">
                <xsl:value-of select="namespacePrefix"/><xsl:value-of select="$propertyName"/>
              </xsl:variable>
              <xsl:variable name="parentPropertyInstanceName"><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:variable>
                <xsl:if test="@attribute">
                if(<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>)
                {
                <xsl:choose>
                <xsl:when test="$nativePropertyType='axiom_attribute_t*' and @isarray"><!-- for anyAttribute -->
                    int i = 0;
                    for( i = 0; i &lt; axutil_array_list_size(<xsl:value-of select="$parentPropertyInstanceName"/>, env); i ++)
                    {
                        axiom_attribute_t *the_attrib = NULL;
                        axiom_attribute_t *dup_attrib = NULL;
                        axis2_char_t *uri = NULL;
                        axis2_char_t *p_prefix = NULL;
                        axutil_qname_t *qname = NULL;
                        axis2_char_t *value = NULL;
                        axis2_char_t *local_name = NULL;

                        the_attrib = axutil_array_list_get(<xsl:value-of select="$parentPropertyInstanceName"/>, env, i);
                        qname = axiom_attribute_get_qname(the_attrib, env);
                        uri = axutil_qname_get_uri(qname, env);
                        value = axiom_attribute_get_value(the_attrib, env);
                        local_name = axutil_qname_get_localpart(qname, env);

                        p_prefix = NULL;
                        if(uri) /* means we have to go for a prefix */
                        {
                            if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, uri, AXIS2_HASH_KEY_STRING)))
                            {
                                p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                                sprintf(p_prefix, "n%d", (*next_ns_index)++);
                                axutil_hash_set(namespaces, uri, AXIS2_HASH_KEY_STRING, p_prefix);
                                axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                         uri,
                                                         p_prefix));
                                 
                            }

                        }

                        text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                         (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                          axutil_strlen(local_name) + 
                                                             axutil_strlen(value)));
                        sprintf(text_value, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                             local_name, value);
                        axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                        AXIS2_FREE(env-> allocator, text_value);
                    }
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                      <xsl:when test="@nsuri and @nsuri != ''">
                        if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING)))
                        {
                            p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                            sprintf(p_prefix, "n%d", (*next_ns_index)++);
                            axutil_hash_set(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING, p_prefix);
                            axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                     "<xsl:value-of select="@nsuri"/>",
                                                     p_prefix));
                        }
                      </xsl:when>
                      <xsl:otherwise>
                        p_prefix = NULL;
                      </xsl:otherwise>
                    </xsl:choose>

                      <!-- here only simple type possible -->
                      <!-- ADB_DEFAULT_DIGIT_LIMIT (64) bytes is used to the store the string representation of the number and the namespace prefix + ":" -->
                      <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5 + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                                                            <!-- here axutil_strlen(":=\"\"") + 1(for NULL terminator) = 5 -->
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>
                        <!-- add axis2_byte_t s -->
                        <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix  &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%c\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%d\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int64_t s -->
                        <xsl:when test="$nativePropertyType='int64_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"" AXIS2_PRINTF_INT64_FORMAT_SPECIFIER  "\"", p_prefix?p_prefix:"", 
                                                (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='uint64_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"" AXIS2_PRINTF_UINT64_FORMAT_SPECIFIER  "\"", p_prefix?p_prefix:"", 
                                                (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%f\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (ADB_DEFAULT_DIGIT_LIMIT + 5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%f\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * 
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(<xsl:value-of select="$propertyInstanceName"/>) + 
                                                                axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(text_value, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>", <xsl:value-of select="$propertyInstanceName"/>);
                           axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           text_value = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>

                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           qname_uri =  axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                           if(qname_uri &amp;&amp; !axutil_strcmp(qname_uri, ""))
                           {
                               if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                               {
                                   qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                                   sprintf(qname_prefix, "q%d", (*next_ns_index) ++); <!-- just different prefix for the special case -->
                                   axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);

                                   axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                                                        qname_uri,
                                                                                        qname_prefix));
                               }
                               text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                                 (2 + axutil_strlen(qname_prefix) +
                                                                    axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env))));
                               sprintf(text_value, "%s%s%s", qname_prefix, (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                           axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                           }
                           else
                           {
                               text_value = (axis2_char_t*)axutil_strdup(env, axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));
                           }

                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);

                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env->allocator, string_to_stream);
                           AXIS2_FREE(env->allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                           text_value = (axis2_char_t*)((<xsl:value-of select="$propertyInstanceName"/>)?"true":"false");
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>
                        <!-- add axis2_date_time_t s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           text_value =  axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>
                        <!-- add axis2_base64 _binary_t s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           text_value =  axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                        </xsl:when>
                        <xsl:when test="@ours">
        
                           <xsl:value-of select="substring-before(@type, '_t*')"/>_declare_parent_namespaces(<xsl:value-of select="$propertyInstanceName"/>,
                                                                                      env, parent_element, namespaces, next_ns_index);
                           text_value = <xsl:value-of select="substring-before(@type, '_t*')"/>_serialize_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, namespaces);
                           string_to_stream = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) *
                                                            (5  + ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(text_value) + 
                                                             axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                           sprintf(string_to_stream, " %s%s%s=\"%s\"", p_prefix?p_prefix:"", (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"",
                                                "<xsl:value-of select="$propertyName"/>",  text_value);
                           axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
                           AXIS2_FREE(env-> allocator, string_to_stream);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /* can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                           text_value = NULL;
                        </xsl:otherwise>
                      </xsl:choose>
                     </xsl:otherwise> <!-- whether this is an anyAttribute or not -->
                    </xsl:choose>
                   }
                   <xsl:if test="not(@optional)">
                   <xsl:if test="not($nativePropertyType='axiom_attribute_t*' and @isarray)"><!-- for anyAttribute -->
                   else
                   {
                      AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Nil value found in non-optional attribute <xsl:value-of select="$propertyName"/>");
                      return NULL;
                   }
                   </xsl:if>
                   </xsl:if>
                </xsl:if> <!-- if for attribute, -->
            </xsl:for-each>


            <xsl:if test="@type">
              <!-- now put the xsi:type to identify the types on polymorphic cases -->
 
              if(!(xsi_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "http://www.w3.org/2001/XMLSchema-instance", AXIS2_HASH_KEY_STRING)))
              {
                  /* it is better to stick with the standard prefix */
                  xsi_prefix = (axis2_char_t*)axutil_strdup(env, "xsi");
                  
                  axutil_hash_set(namespaces, "http://www.w3.org/2001/XMLSchema-instance", AXIS2_HASH_KEY_STRING, xsi_prefix);

                  if(parent_element)
                  {
                        axiom_namespace_t *element_ns = NULL;
                        element_ns = axiom_namespace_create(env, "http://www.w3.org/2001/XMLSchema-instance",
                                                            xsi_prefix);
                        axiom_element_declare_namespace_assume_param_ownership(parent_element, env, element_ns);
                  }
              }
              type_attrib = axutil_strcat(env, " ", xsi_prefix, ":type=\"<xsl:value-of select="$type_name"/>\"", NULL);
              axutil_stream_write(stream, env, type_attrib, axutil_strlen(type_attrib));

              AXIS2_FREE(env->allocator, type_attrib);
                
              string_to_stream = "&gt;"; <!-- The ending tag of the parent -->
              axutil_stream_write(stream, env, string_to_stream, axutil_strlen(string_to_stream));
              tag_closed = 1;
            </xsl:if>

             <!-- end bracket for if(!parent_tag_closed)-->
            <xsl:if test="count(property[@attribute])!=0 or @type">
            }
            else {
              /* if the parent tag closed we would be able to declare the type directly on the parent element */ 
              if(!(xsi_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "http://www.w3.org/2001/XMLSchema-instance", AXIS2_HASH_KEY_STRING)))
              {
                  /* it is better to stick with the standard prefix */
                  xsi_prefix = (axis2_char_t*)axutil_strdup(env, "xsi");
                  
                  axutil_hash_set(namespaces, "http://www.w3.org/2001/XMLSchema-instance", AXIS2_HASH_KEY_STRING, xsi_prefix);

                  if(parent_element)
                  {
                        axiom_namespace_t *element_ns = NULL;
                        element_ns = axiom_namespace_create(env, "http://www.w3.org/2001/XMLSchema-instance",
                                                            xsi_prefix);
                        axiom_element_declare_namespace_assume_param_ownership(parent_element, env, element_ns);
                  }
              }
            }
            xsi_ns = axiom_namespace_create (env,
                                 "<xsl:value-of select="@nsuri"/>",
                                 xsi_prefix);
            xsi_type_attri = axiom_attribute_create (env, "type", "<xsl:value-of select="$type_name"/>", xsi_ns);
            <!-- TODO: parent here can be data_source node, not element node should be fixed -->
            axiom_element_add_attribute (parent_element, env, xsi_type_attri, parent);
        </xsl:if>

            <xsl:if test="$isUnion">
            /* here we need to declare the union type in the xsi:type field */
            
            if(axutil_strcmp(<xsl:value-of select="$name"/>->current_value, ""))
            {

                if(!(xsi_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "http://www.w3.org/2001/XMLSchema-instance", AXIS2_HASH_KEY_STRING)))
                {
                    /* it is better to stick with the standard prefix */
                    xsi_prefix = (axis2_char_t*)axutil_strdup(env, "xsi");
                    
                    axutil_hash_set(namespaces, "http://www.w3.org/2001/XMLSchema-instance", AXIS2_HASH_KEY_STRING, xsi_prefix);

                    if(parent_element)
                    {
                          axiom_namespace_t *element_ns = NULL;
                          element_ns = axiom_namespace_create(env, "http://www.w3.org/2001/XMLSchema-instance",
                                                              xsi_prefix);
                          axiom_element_declare_namespace_assume_param_ownership(parent_element, env, element_ns);
                    }
                }

                <xsl:for-each select="memberType">
                    <xsl:variable name="member_type" select="@type"/>
                    <xsl:variable name="member_type_ns" select="@nsuri"/>
                    <xsl:variable name="member_name"><xsl:text></xsl:text><xsl:value-of select="@originalName"/></xsl:variable>

                    if(!axutil_strcmp(<xsl:value-of select="$name"/>->current_value, "<xsl:value-of select="@originalName"/>"))
                    {
                        axis2_char_t *ns_prefix = NULL;

                        if(!(ns_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "<xsl:value-of select="$member_type_ns"/>", AXIS2_HASH_KEY_STRING)))
                        {
                            ns_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                            
                            sprintf(ns_prefix, "q%d", (*next_ns_index)++); <!-- just different prefix for the special case -->
                            axutil_hash_set(namespaces, "<xsl:value-of select="$member_type_ns"/>", AXIS2_HASH_KEY_STRING, ns_prefix);

                            if(parent_element)
                            {
                                  axiom_namespace_t *element_ns = NULL;
                                  element_ns = axiom_namespace_create(env, "<xsl:value-of select="$member_type_ns"/>",
                                                                      ns_prefix);
                                  axiom_element_declare_namespace_assume_param_ownership(parent_element, env, element_ns);
                            }
                        }

                        /* now we will set the xsi:type="ns:type" value */

                       if(!parent_tag_closed &amp;&amp; !tag_closed)
                       {
                            text_value = axutil_strcat(env, xsi_prefix, ":type=", ns_prefix, ":", <xsl:value-of select="$name"/>->current_value, NULL);
                            axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));

                            AXIS2_FREE(env->allocator, text_value);
                        }
                        else 
                        {
                            /* otherwise set it to the prarent itself */

                             axiom_namespace_t *ns1 = NULL;
                             axiom_attribute_t *attrib = NULL;
                        
                             ns1 = axiom_namespace_create (env,
                                                         "http://www.w3.org/2001/XMLSchema-instance",
                                                         xsi_prefix);
                        
                             text_value = axutil_strcat(env, ns_prefix, ":", <xsl:value-of select="$name"/>->current_value, NULL);
                             attrib = axiom_attribute_create (env, "type", text_value, ns1);
                             axiom_element_add_attribute (parent_element, env, attrib, parent);
                            
                             AXIS2_FREE(env->allocator, text_value);
                        }
                    }
                </xsl:for-each>
            }
            </xsl:if>

            <xsl:if test="@simple">
               if(!parent_tag_closed &amp;&amp; !tag_closed)
               {
                  text_value = "&gt;"; <!-- The ending tag of the parent -->
                  axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
               }
               <!-- how if this type is a qname :(, simply we are not handling that situation.. -->
               text_value = <xsl:value-of select="$axis2_name"/>_serialize_to_string(<xsl:value-of select="$name"/>, env, namespaces);
               if(text_value)
               {
                    axutil_stream_write(stream, env, text_value, axutil_strlen(text_value));
                    AXIS2_FREE(env->allocator, text_value);
               }
            </xsl:if>

            <xsl:for-each select="property">
              <xsl:variable name="position"><xsl:value-of select="position()"/></xsl:variable>
              <xsl:variable name="propertyType">
                 <xsl:choose>
                   <xsl:when test="@isarray">axutil_array_list_t*</xsl:when>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="nativePropertyType"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:otherwise><xsl:value-of select="@type"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="propertyName"><xsl:value-of select="@originalName"></xsl:value-of></xsl:variable>
              <xsl:variable name="CName"><xsl:value-of select="@cname"></xsl:value-of></xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
              <xsl:variable name="parentPropertyInstanceName"><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:variable>

              <xsl:choose>
                <xsl:when test="@attribute">
                    <!-- here only simple type possible -->
                    if(parent_tag_closed)
                    {
                       if(<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>)
                       {
                       <xsl:choose>
                       <xsl:when test="$nativePropertyType='axiom_attribute_t*' and @isarray"><!-- for anyAttribute -->
                        int i = 0;
                        for( i = 0; i &lt; axutil_array_list_size(<xsl:value-of select="$parentPropertyInstanceName"/>, env); i ++)
                        {
                            axiom_attribute_t *the_attrib = NULL;
                            axiom_attribute_t *dup_attrib = NULL;
                            axis2_char_t *uri = NULL;
                            axis2_char_t *p_prefix = NULL;
                            axutil_qname_t *qname = NULL;
                            axis2_char_t *value = NULL;
                            axis2_char_t *local_name = NULL;
                            axiom_namespace_t *ns1 = NULL;

                            the_attrib = axutil_array_list_get(<xsl:value-of select="$parentPropertyInstanceName"/>, env, i);
                            qname = axiom_attribute_get_qname(the_attrib, env);
                            uri = axutil_qname_get_uri(qname, env);
                            value = axiom_attribute_get_value(the_attrib, env);
                            local_name = axutil_qname_get_localpart(qname, env);

                            p_prefix = NULL;
                            if(uri) /* means we have to go for a prefix */
                            {
                                if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, uri, AXIS2_HASH_KEY_STRING)))
                                {
                                    p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                                    sprintf(p_prefix, "n%d", (*next_ns_index)++);
                                    axutil_hash_set(namespaces, uri, AXIS2_HASH_KEY_STRING, p_prefix);
                                    axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                             uri,
                                                             p_prefix));
                                     
                                }

                            }

                            ns1 = axiom_namespace_create (env,
                                                uri,
                                                p_prefix);

                            dup_attrib = axiom_attribute_create (env, local_name, value, ns1);
                            <!-- TODO: parent here can be data_source node, not element node should be fixed -->
                            axiom_element_add_attribute (parent_element, env, dup_attrib, parent);

                        }
                       </xsl:when>
                       <xsl:otherwise>
                       <xsl:choose>
                         <xsl:when test="@nsuri and @nsuri != ''">
                           if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING)))
                           {
                               p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                               sprintf(p_prefix, "n%d", (*next_ns_index)++);
                               axutil_hash_set(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING, p_prefix);
                               
                               axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                "<xsl:value-of select="@nsuri"/>",
                                                p_prefix));
                           }
                           ns1 = axiom_namespace_create (env,
                                                "<xsl:value-of select="@nsuri"/>",
                                                p_prefix);
                         </xsl:when>
                         <xsl:otherwise>
                           p_prefix = NULL;
                           ns1 = NULL;
                         </xsl:otherwise>
                       </xsl:choose> <!-- close for test nsuri and nsuri != "" -->

                       <xsl:choose>
                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int' or $nativePropertyType='unsigned int'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           <!-- TODO: parent here can be data_source node, not element node should be fixed -->
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_byte_t s -->
                        <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='char' or $nativePropertyType='unsigned char'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%c", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short' or $nativePropertyType='unsigned short'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add int64_t s -->
                        <xsl:when test="$nativePropertyType='int64_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, AXIS2_PRINTF_INT64_FORMAT_SPECIFIER, (int64_t)<xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='uint64_t'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, AXIS2_PRINTF_UINT64_FORMAT_SPECIFIER, (uint64_t)<xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           text_value = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, sizeof (axis2_char_t) * ADB_DEFAULT_DIGIT_LIMIT);
                           sprintf (text_value, "%f", <xsl:value-of select="$propertyInstanceName"/>);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env-> allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value = <xsl:value-of select="$propertyInstanceName"/>;
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           text_value = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>

                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">

                           qname_uri =  axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                           if(qname_uri)
                           {
                               if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                               {

                                   qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                                   sprintf(qname_prefix, "q%d", (*next_ns_index) ++ ); <!-- just different prefix for the special case -->

                                   axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);
                                   
                                   axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                                     qname_uri,
                                                     qname_prefix));
                               }
                           }

                           text_value = (axis2_char_t*) AXIS2_MALLOC(env-> allocator, 
                                         sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env)) + 2));
                           sprintf(text_value, "%s%s%s", qname_uri?qname_prefix:"",
                                                        qname_uri?":":"",
                                                       axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));

                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env->allocator, text_value);
                        </xsl:when>

                        <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           <!--text_value = (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false";-->
                           text_value =  (axis2_char_t*)((<xsl:value-of select="$propertyInstanceName"/>)?axutil_strdup(env, "true"):axutil_strdup(env, "false"));
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                           AXIS2_FREE(env->allocator, text_value);
                        </xsl:when>
                        <!-- add axis2_date_time_t s -->
                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                           text_value =  axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>
                       <!-- add axis2_base64_binary_t s -->
                        <xsl:when test="$nativePropertyType='axutil_base64_binary_t*'">
                           text_value =  axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                           text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                           axiom_element_add_attribute (parent_element, env, text_attri, parent);
                        </xsl:when>
                        <xsl:when test="@ours">
                           <xsl:value-of select="substring-before(@type, '_t*')"/>_declare_parent_namespaces(<xsl:value-of select="$propertyInstanceName"/>,
                                                                                      env, parent_element, namespaces, next_ns_index);
                           text_value = <xsl:value-of select="substring-before(@type, '_t*')"/>_serialize_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, namespaces);
                           if(text_value)
                           {
                               text_attri = axiom_attribute_create (env, "<xsl:value-of select="$propertyName"/>", text_value, ns1);
                               axiom_element_add_attribute (parent_element, env, text_attri, parent);
                               AXIS2_FREE(env-> allocator, text_value);
                           }
                        </xsl:when>
                        <xsl:otherwise>
                          <!--TODO: add new attributes types -->
                          /* Can not handle the attribute type <xsl:value-of select="$nativePropertyType"/>*/
                           text_value = NULL;
                           parent_element = NULL;
                           text_attri = NULL;
                        </xsl:otherwise>
                        </xsl:choose>
                       </xsl:otherwise>
                       </xsl:choose>
                      }
                      <xsl:if test="not(@optional)">
                      <xsl:if test="not($nativePropertyType='axiom_attribute_t*' and @isarray)"><!-- for anyAttribute -->
                      else
                      {
                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Nil value found in non-optional attribute <xsl:value-of select="$propertyName"/>");
                         return NULL;
                      }
                      </xsl:if> 
                      </xsl:if> 
                  }<!-- End bracket for if(parent_tag_closed)-->
                </xsl:when>
                <xsl:when test="$simple"></xsl:when> <!--Just to ignore parsing following code at simple types-->
                <xsl:otherwise>
 
                <xsl:if test="$choice">
                if(0 == axutil_strcmp(<xsl:value-of select="$name"/>->current_choice, "<xsl:value-of select="@nsuri"/>:<xsl:value-of select="$propertyName"/>"))
                {
                </xsl:if>


                   <xsl:choose>
                     <xsl:when test="@nsuri and @nsuri != ''">
                       if(!(p_prefix = (axis2_char_t*)axutil_hash_get(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING)))
                       {
                           p_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);
                           sprintf(p_prefix, "n%d", (*next_ns_index)++);
                           axutil_hash_set(namespaces, "<xsl:value-of select="@nsuri"/>", AXIS2_HASH_KEY_STRING, p_prefix);
                           
                           axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                            "<xsl:value-of select="@nsuri"/>",
                                            p_prefix));
                       }
                     </xsl:when>
                     <xsl:otherwise>
                       p_prefix = NULL;
                     </xsl:otherwise>
                   </xsl:choose> <!-- close for test nsuri and nsuri != "" -->

                   if (!<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>)
                   {
                      <xsl:if test="@nillable">
                        <xsl:choose>
                          <xsl:when test="@minOccurs=0">
                           /* no need to complain for minoccurs=0 element */
                            <!-- just ignore the element.. -->
                          </xsl:when>
                          <xsl:otherwise>
                            <!-- just write a nil element -->
                            start_input_str = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof(axis2_char_t) *
                                        (5 + axutil_strlen(p_prefix) + 
                                         axutil_strlen("<xsl:value-of select="$propertyName"/>") + 
                                         axutil_strlen(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"1\""))); 
                                        <!-- axutil_strlen("<:/>") + 1 = 5 -->
                            
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/> xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"1\"/&gt;",
                                        p_prefix?p_prefix:"",
                                        (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                                        
                            axutil_stream_write(stream, env, start_input_str, axutil_strlen(start_input_str));
                            AXIS2_FREE(env->allocator,start_input_str);
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:if>

                      <xsl:if test="not(@nillable)">
                        <xsl:choose>
                          <xsl:when test="@minOccurs=0">
                           /* no need to complain for minoccurs=0 element */
                            <!-- just ignore the element.. -->
                          </xsl:when>
                          <xsl:otherwise>
                            <!-- just return an error -->
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Nil value found in non-nillable property <xsl:value-of select="$propertyName"/>");
                            return NULL;
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:if>
                   }
                   else
                   {
                     start_input_str = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof(axis2_char_t) *
                                 (4 + axutil_strlen(p_prefix) + 
                                  axutil_strlen("<xsl:value-of select="$propertyName"/>"))); 
                                 <!-- axutil_strlen("<:>") + 1 = 4 -->
                                 /* axutil_strlen("&lt;:&gt;") + 1 = 4 */
                     end_input_str = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof(axis2_char_t) *
                                 (5 + axutil_strlen(p_prefix) + axutil_strlen("<xsl:value-of select="$propertyName"/>")));
                                  /* axutil_strlen("&lt;/:&gt;") + 1 = 5 */
                                  <!-- axutil_strlen("</:>") + 1 = 5 -->
                     

                   <!-- handles arrays -->
                   <xsl:if test="@isarray">
                     /*
                      * Parsing <xsl:value-of select="$CName"/> array
                      */
                     if (<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> != NULL)
                     {
                        <xsl:choose>
                            <xsl:when test="@ours">

                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                            </xsl:when>
                            <xsl:otherwise>
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":""); 
                            </xsl:otherwise>
                        </xsl:choose>
                         start_input_str_len = axutil_strlen(start_input_str);

                         sprintf(end_input_str, "&lt;/%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                         end_input_str_len = axutil_strlen(end_input_str);

                         count = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                         for(i = 0; i &lt; count; i ++)
                         {
                            element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);

                            if(NULL == element) <!--validty of individual -->
                            {
                                continue;
                            }
                    </xsl:if>
                     <!-- for each non attribute properties there will always be an element-->
                     /*
                      * parsing <xsl:value-of select="$propertyName"/> element
                      */

                    <!-- how to build all the ours things -->
                    <xsl:if test="not(@isarray)">
                        <xsl:choose>
                            <xsl:when test="@ours">
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":""); 
                            </xsl:when>
                            <xsl:otherwise>
                            sprintf(start_input_str, "&lt;%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                            </xsl:otherwise>
                        </xsl:choose>
                        start_input_str_len = axutil_strlen(start_input_str);
                        sprintf(end_input_str, "&lt;/%s%s<xsl:value-of select="$propertyName"/>&gt;",
                                 p_prefix?p_prefix:"",
                                 (p_prefix &amp;&amp; axutil_strcmp(p_prefix, ""))?":":"");
                        end_input_str_len = axutil_strlen(end_input_str);
                    </xsl:if>


                      <xsl:choose>
                        <xsl:when test="@ours">
                            <xsl:if test="$anon or $istype"> <!-- As this shows, elements are not writing their tags here from stream.
                                                                 It is done using axiom manipualation above..-->
                            if(!<xsl:value-of select="substring-before(@type, '_t*')"/>_is_particle())
                            {
                                axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                            }
                            </xsl:if>
                            
                            <xsl:variable name="element_closed">
                                <xsl:choose>
                                    <xsl:when test="../@type">AXIS2_FALSE</xsl:when>
                                    <!-- this mean the anonymous header is writing -->
                                    <xsl:when test="$anon or $istype">AXIS2_FALSE</xsl:when>
                                    <xsl:otherwise>AXIS2_TRUE</xsl:otherwise>
                                </xsl:choose>
                            </xsl:variable>
                            <xsl:value-of select="substring-before(@type, '_t*')"/>_serialize(<xsl:value-of select="$propertyInstanceName"/>, <!-- This will be either element (in array) or just the property -->
                                                                                 env, current_node, parent_element,
                                                                                 <xsl:value-of select="substring-before(@type, '_t*')"/>_is_particle() || <xsl:value-of select="$element_closed"/>, namespaces, next_ns_index);
                            <xsl:if test="$anon or $istype">
                            if(!<xsl:value-of select="substring-before(@type, '_t*')"/>_is_particle())
                            {
                                axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                            }
                            </xsl:if>
                        </xsl:when>

                        <!-- add int s -->
                        <xsl:when test="$nativePropertyType='int'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_INT32_FORMAT_SPECIFIER, *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_INT32_FORMAT_SPECIFIER, <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned int'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_UINT32_FORMAT_SPECIFIER, *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_UINT32_FORMAT_SPECIFIER, <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add char s -->
                        <xsl:when test="$nativePropertyType='char'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%c", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%c", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned char'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%c", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%c", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_unsigned_byte_t'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add short s -->
                        <xsl:when test="$nativePropertyType='short'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%d", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='unsigned short'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%hu", *((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%hu", <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>


                        <!-- NOTE: arrays for int64_t, float, int are handled differently. they are stored in pointers -->
                        <!-- add int64_t s -->
                        <xsl:when test="$nativePropertyType='int64_t'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_INT64_FORMAT_SPECIFIER, (int64_t)*((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_INT64_FORMAT_SPECIFIER, (int64_t) <xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add int64_t s -->
                        <xsl:when test="$nativePropertyType='uint64_t'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_UINT64_FORMAT_SPECIFIER, (uint64_t)*((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, AXIS2_PRINTF_UINT64_FORMAT_SPECIFIER, (uint64_t)<xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add float s -->
                        <xsl:when test="$nativePropertyType='float'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", (float)*((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", (float)<xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add double s -->
                        <xsl:when test="$nativePropertyType='double'">
                           <xsl:choose>
                             <xsl:when test="@isarray">
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", (double)*((<xsl:value-of select="$nativePropertyType"/>*)element));
                             </xsl:when>
                             <xsl:otherwise>
                               sprintf (text_value_<xsl:value-of select="$position"/>, "%f", (double)<xsl:value-of select="$propertyInstanceName"/>);
                             </xsl:otherwise>
                           </xsl:choose>
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axis2_char_t s -->
                        <xsl:when test="$nativePropertyType='axis2_char_t*'">
                           text_value_<xsl:value-of select="$position"/> = <xsl:value-of select="$propertyInstanceName"/>;
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                            
                           text_value_<xsl:value-of select="$position"/>_temp = axutil_xml_quote_string(env, text_value_<xsl:value-of select="$position"/>, AXIS2_TRUE);
                           if (text_value_<xsl:value-of select="$position"/>_temp)
                           {
                               axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>_temp, axutil_strlen(text_value_<xsl:value-of select="$position"/>_temp));
                               AXIS2_FREE(env->allocator, text_value_<xsl:value-of select="$position"/>_temp);
                           }
                           else
                           {
                               axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           }
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axutil_uri_t s -->
                        <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                           text_value_<xsl:value-of select="$position"/> = axutil_uri_to_string(<xsl:value-of select="$propertyInstanceName"/>, env, AXIS2_URI_UNP_OMITUSERINFO);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axutil_duration_t s -->
                        <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                           text_value_<xsl:value-of select="$position"/> = axutil_duration_serialize_duration(<xsl:value-of select="$propertyInstanceName"/>, env);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add axutil_qname_t s -->
                        <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                           <!-- Handled above -->
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>

                           <!-- TODO: Do this in single step -->

                           qname_uri =  axutil_qname_get_uri(<xsl:value-of select="$propertyInstanceName"/>, env);
                           if(qname_uri)
                           {
                               if(!(qname_prefix = (axis2_char_t*)axutil_hash_get(namespaces, qname_uri, AXIS2_HASH_KEY_STRING)))
                               {

                                   qname_prefix = (axis2_char_t*)AXIS2_MALLOC(env->allocator, sizeof (axis2_char_t) * ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT);

                                   sprintf(qname_prefix, "q%d", (*next_ns_index) ++ ); <!-- just different prefix for the special case -->

                                   axutil_hash_set(namespaces, qname_uri, AXIS2_HASH_KEY_STRING, qname_prefix);
                                   axiom_element_declare_namespace_assume_param_ownership(parent_element, env, axiom_namespace_create (env,
                                            qname_uri,
                                            qname_prefix));
                               }
                           }

                           text_value_<xsl:value-of select="$position"/> = (axis2_char_t*) AXIS2_MALLOC (env-> allocator, 
                                         sizeof (axis2_char_t) * (ADB_DEFAULT_NAMESPACE_PREFIX_LIMIT +
                                                             axutil_strlen(axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env)) + 2));

                           sprintf(text_value_<xsl:value-of select="$position"/>, "%s%s%s",
                                                       qname_uri?qname_prefix:"",
                                                       qname_uri?":":"",
                                                       axutil_qname_get_localpart(<xsl:value-of select="$propertyInstanceName"/>, env));

                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           AXIS2_FREE(env-> allocator, text_value_<xsl:value-of select="$position"/>);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                          <!-- add axis2_bool_t s -->
                        <xsl:when test="$nativePropertyType='axis2_bool_t'">
                           strcpy(text_value_<xsl:value-of select="$position"/>, (<xsl:value-of select="$propertyInstanceName"/>)?"true":"false");
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!-- add nodes -->
                        <xsl:when test="$nativePropertyType='axiom_node_t*'">
                           <xsl:choose>
                              <xsl:when test="$anon or $istype">
                                text_value_<xsl:value-of select="$position"/> = axiom_node_to_string(<xsl:value-of select="$propertyInstanceName"/>, env);
                                <xsl:if test="not(@any)">
                                axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                                </xsl:if>
                                axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                                <xsl:if test="not(@any)">
                                axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                                </xsl:if>
                              </xsl:when>
                              <xsl:otherwise>
                                text_value_<xsl:value-of select="$position"/> = NULL; /* just to bypass the warning unused variable */
                                axiom_node_add_child(parent, env, <xsl:value-of select="$propertyInstanceName"/>);
                              </xsl:otherwise>
                           </xsl:choose>
                        </xsl:when>

                        <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                          text_value_<xsl:value-of select="$position"/> = axutil_date_time_serialize_date_time(<xsl:value-of select="$propertyInstanceName"/>, env);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                          text_value_<xsl:value-of select="$position"/> =axutil_base64_binary_get_encoded_binary(<xsl:value-of select="$propertyInstanceName"/>, env);
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, start_input_str, start_input_str_len);
                           </xsl:if>
                           axutil_stream_write(stream, env, text_value_<xsl:value-of select="$position"/>, axutil_strlen(text_value_<xsl:value-of select="$position"/>));
                           <xsl:if test="$anon or $istype">
                           axutil_stream_write(stream, env, end_input_str, end_input_str_len);
                           </xsl:if>
                        </xsl:when>

                        <!--TODO: This should be extended for all the types that should be freed.. -->
                        <xsl:otherwise>
                          /* This is an unknown type or a primitive. handle this manually for unknown type */
                        </xsl:otherwise>
                      </xsl:choose>

                   <!-- close tags arrays -->
                   <xsl:if test="@isarray">
                         }
                     }
                   </xsl:if>
                     <!-- This two should be freed after the loop for array-->
                     AXIS2_FREE(env->allocator,start_input_str);
                     AXIS2_FREE(env->allocator,end_input_str);
                 } <!-- else for non nillable -->

                 <xsl:if test="$choice">
                 }
                 </xsl:if>

                </xsl:otherwise> <!-- othewise for non attributes -->
              </xsl:choose>
            </xsl:for-each>

            <xsl:if test="not(@type) and not(@simple)"> <!-- So this is the root of the serialization call tree -->
              <xsl:for-each select="property">
                <xsl:if test="position()=1">
                   if(namespaces)
                   {
                       axutil_hash_index_t *hi;
                       void *val;
                       for (hi = axutil_hash_first(namespaces, env); hi; hi = axutil_hash_next(env, hi)) 
                       {
                           axutil_hash_this(hi, NULL, NULL, &amp;val);
                           AXIS2_FREE(env->allocator, val);
                       }
                       axutil_hash_free(namespaces, env);
                   }
                </xsl:if>
              </xsl:for-each>
            </xsl:if>

            return parent;
        }


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

              <!-- Just to identiy the pointer to arrays -->
              <!-- Simmilar to native property type except for shor, tint, float, double -->
              <xsl:variable name="PropertyTypeArrayParam"> <!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="not(@type)">axiom_node_t*</xsl:when> <!-- these are anonymous -->
                   <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t' or @type='axis2_bool_t'">
                    <xsl:value-of select="@type"/><xsl:text>*</xsl:text>
                   </xsl:when>
                   <xsl:otherwise>
                    <xsl:value-of select="@type"/>
                   </xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="propertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>

              <xsl:variable name="justPropertyInstanceName"><!--these are used in arrays to take the native type-->
                 <xsl:choose>
                   <xsl:when test="@isarray">element</xsl:when>
                   <xsl:otherwise><xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/></xsl:otherwise>
                 </xsl:choose>
              </xsl:variable>
            <xsl:variable name="constValue">
                <xsl:choose>
                   <xsl:when test="@type='axis2_char_t*' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'">const </xsl:when>
                </xsl:choose>
            </xsl:variable>

            /**
             * Getter for <xsl:value-of select="$propertyName"/> by  Property Number <xsl:value-of select="position()"/>
             */
            <xsl:value-of select="$propertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_property<xsl:value-of select="position()"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env)
            {
                return <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                             env);
            }

            /**
             * getter for <xsl:value-of select="$propertyName"/>.
             */
            <xsl:value-of select="$propertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env)
             {
                <xsl:choose>
                  <xsl:when test="$propertyType='unsigned short' or $propertyType='unsigned char' or $propertyType='unsigned int' or $propertyType='uint64_t' or $propertyType='short' or $propertyType='axis2_unsigned_byte_t' or $propertyType='axis2_byte_t' or $propertyType='axis2_bool_t' or $propertyType='char' or $propertyType='int' or $propertyType='float' or $propertyType='double' or $propertyType='int64_t'">
                    AXIS2_ENV_CHECK(env, (<xsl:value-of select="$propertyType"/>)0);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, (<xsl:value-of select="$propertyType"/>)0);
                  </xsl:when>
                  <xsl:otherwise>
                    AXIS2_ENV_CHECK(env, NULL);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
                  </xsl:otherwise>
                </xsl:choose>

                return <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>;
             }

            /**
             * setter for <xsl:value-of select="$propertyName"/>
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env,
                    <xsl:if test="not(@isarray)"><xsl:value-of select="$constValue"/></xsl:if><xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> arg_<xsl:value-of select="$CName"/>)
             {
                <xsl:if test="@isarray">
                 int size = 0;
                 int i = 0;
                 axis2_bool_t non_nil_exists = AXIS2_FALSE;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
                
                if(<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> &amp;&amp;
                        arg_<xsl:value-of select="$CName"/> == <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>)
                {
                    <xsl:if test="$choice">
                        <xsl:value-of select="$name"/>->current_choice = "<xsl:value-of select="@nsuri"/>:<xsl:value-of select="$propertyName"/>";
                    </xsl:if>
                    return AXIS2_SUCCESS; 
                }

                <xsl:if test="@isarray">
                 size = axutil_array_list_size(arg_<xsl:value-of select="$CName"/>, env);
                 <xsl:if test="not(@unbound)">
                     if (size &gt; <xsl:value-of select="@maxOccurs"/>)
                     {
                         AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> has exceed the maxOccurs(<xsl:value-of select="@maxOccurs"/>)");
                         return AXIS2_FAILURE;
                     }
                 </xsl:if>
                 if (size &lt; <xsl:value-of select="@minOccurs"/>)
                 {
                     AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> has less than minOccurs(<xsl:value-of select="@minOccurs"/>)");
                     return AXIS2_FAILURE;
                 }
                 for(i = 0; i &lt; size; i ++ )
                 {
                     if(NULL != axutil_array_list_get(arg_<xsl:value-of select="$CName"/>, env, i))
                     {
                         non_nil_exists = AXIS2_TRUE;
                         break;
                     }
                 }

                 <xsl:if test ="not(@nillabe) and not(@minOccurs='0')">
                    if(!non_nil_exists)
                    {
                        AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                        return AXIS2_FAILURE;
                    }
                 </xsl:if>
                </xsl:if> <!-- close for the isarray -->

                <xsl:if test="not(@nillable) and not(@minOccurs='0') and (@ours or not($propertyType='unsigned char' or $propertyType='unsigned int' or $propertyType='unsigned short' or $propertyType='uint64_t' or $propertyType='char' or $propertyType='int' or $propertyType='short' or $propertyType='float' or $propertyType='axis2_unsigned_byte_t' or $propertyType='axis2_byte_t' or $propertyType='double' or $propertyType='int64_t' or $propertyType='axis2_bool_t'))">
                  if(NULL == arg_<xsl:value-of select="$CName"/>)
                  {
                      AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "<xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable element");
                      return AXIS2_FAILURE;
                  }
                </xsl:if>

                <!-- first reset whatever already in there -->
                <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);

                <xsl:if test="(@ours or not($propertyType='unsigned char' or $propertyType='unsigned int' or $propertyType='unsigned short' or $propertyType='uint64_t' or $propertyType='char' or $propertyType='int' or $propertyType='short' or $propertyType='float' or $propertyType='axis2_unsigned_byte_t' or $propertyType='axis2_byte_t' or $propertyType='double' or $propertyType='int64_t' or $propertyType='axis2_bool_t'))">
                if(NULL == arg_<xsl:value-of select="$CName"/>)
                {
                    /* We are already done */
                    return AXIS2_SUCCESS;
                }
                </xsl:if>

                <xsl:choose>
                    <xsl:when test="@isarray">
                        <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = arg_<xsl:value-of select="$CName"/>;
                        if(non_nil_exists)
                        {
                            <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                        }
                        <!-- else is_valid_* = AXIS2_FALSE is set by the above reset function -->
                    </xsl:when>
                    <xsl:when test="@type='axis2_char_t*' and not(@isarray)">
                        <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = (axis2_char_t *)axutil_strdup(env, arg_<xsl:value-of select="$CName"/>);
                        if(NULL == <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>)
                        {
                            AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Error allocating memeory for <xsl:value-of select="$propertyName"/>");
                            return AXIS2_FAILURE;
                        }
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = arg_<xsl:value-of select="$CName"/>;
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$choice">
                    <xsl:value-of select="$name"/>->current_choice = "<xsl:value-of select="@nsuri"/>:<xsl:value-of select="$propertyName"/>";
                </xsl:if>
                return AXIS2_SUCCESS;
             }

            <xsl:if test="$isEnum=1">
               <xsl:variable name="enum">adb_<xsl:value-of select="$propertyName"/>_enum_t</xsl:variable>
             /**
             * specialized enum getter for <xsl:value-of select="$propertyName"/>.
             */
             <xsl:value-of select="$enum"/> AXIS2_CALL
             <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_enum(
                 <xsl:value-of select="$axis2_name"/>_t* <xsl:value-of select="$name"/>,
                 const axutil_env_t *env)
             {
                 AXIS2_ENV_CHECK(env, -1);
                 AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, -1);
             
             <xsl:for-each select="enumFacet">
                 if (axutil_strcmp(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, "<xsl:value-of select="@value"/>") == 0)
                    return <xsl:value-of select="parent::node()/@caps-cname"/>_<xsl:value-of select="@id"/>;
             </xsl:for-each>
             
                 /* Error: none of the strings matched; invalid enum value */
                 return -1;
             }
             
             
             /**
             * specialized enum setter for <xsl:value-of select="$propertyName"/>.
             */
             axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_enum(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env,
                    <xsl:value-of select="$constValue"/><xsl:value-of select="$enum"/><xsl:text> </xsl:text> arg_<xsl:value-of select="$CName"/>)
             {
                <xsl:if test="@isarray">
                 int size = 0;
                 int i = 0;
                 axis2_bool_t non_nil_exists = AXIS2_FALSE;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

                <!-- first reset whatever already in there -->
                   <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);

                   
                   switch (arg_<xsl:value-of select="$CName"/>)
                   {
                     <xsl:for-each select="enumFacet">
                       case <xsl:value-of select="parent::node()/@caps-cname"/>_<xsl:value-of select="@id"/> :
                          <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = (axis2_char_t *)axutil_strdup(env, "<xsl:value-of select="@value"/>");
                          break;
                     </xsl:for-each>
                     
                       default:
                          <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                          <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = NULL;
                          AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Error setting <xsl:value-of select="$propertyName"/>: undefined enum value");
                          return AXIS2_FAILURE;
                   }
                
                   if(NULL == <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Error allocating memory for <xsl:value-of select="$propertyName"/>");
                       return AXIS2_FAILURE;
                   }
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                        
                <xsl:if test="$choice">
                    <xsl:value-of select="$name"/>->current_choice = "<xsl:value-of select="@nsuri"/>:<xsl:value-of select="$propertyName"/>";
                </xsl:if>
                return AXIS2_SUCCESS;
             }
            </xsl:if>

            <xsl:if test="@isarray">
            /**
             * Get ith element of <xsl:value-of select="$propertyName"/>.
             */
            <xsl:value-of select="$nativePropertyType"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, int i)
            {
                <xsl:value-of select="$PropertyTypeArrayParam"/> ret_val;

                <xsl:choose>
                  <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                    AXIS2_ENV_CHECK(env, (<xsl:value-of select="$nativePropertyType"/>)0);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, (<xsl:value-of select="$nativePropertyType"/>)0);
                  </xsl:when>
                  <xsl:otherwise>
                    AXIS2_ENV_CHECK(env, NULL);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
                  </xsl:otherwise>
                </xsl:choose>

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    return (<xsl:value-of select="$nativePropertyType"/>)0;
                }
                ret_val = (<xsl:value-of select="$PropertyTypeArrayParam"/>)axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t' or @type='axis2_bool_t'">
                    if(ret_val)
                    {
                        return *ret_val;
                    }
                    return (<xsl:value-of select="$nativePropertyType"/>)0;
                  </xsl:when>
                  <xsl:otherwise>
                    return ret_val;
                  </xsl:otherwise>
                </xsl:choose>
            }

            /**
             * Set the ith element of <xsl:value-of select="$propertyName"/>.
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_at(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, int i,
                    <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text><xsl:value-of select="$CName"/>)
            {
                void *element = NULL;
                int size = 0;
                int j;
                int non_nil_count;
                axis2_bool_t non_nil_exists = AXIS2_FALSE;

                <xsl:if test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or  @type='axis2_byte_t'">
                   <xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text> ptr_param_</xsl:text><xsl:value-of select="$CName"/>;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
                
                if( <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> &amp;&amp;
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> &amp;&amp;
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                    arg_<xsl:value-of select="$CName"/> == *((<xsl:value-of select="$PropertyTypeArrayParam"/>)axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i)))
                  </xsl:when>
                  <xsl:otherwise>
                    arg_<xsl:value-of select="$CName"/> == (<xsl:value-of select="$nativePropertyType"/>)axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i))
                  </xsl:otherwise>
                </xsl:choose>
                {
                    <xsl:if test="$choice">
                        <xsl:value-of select="$name"/>->current_choice = "<xsl:value-of select="@nsuri"/>:<xsl:value-of select="$propertyName"/>";
                    </xsl:if>
                    return AXIS2_SUCCESS; 
                }

                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                     non_nil_exists = AXIS2_TRUE; /* no way to check for nill for each elements for primitive types */
                  </xsl:when>
                  <xsl:otherwise>
                    if(NULL != arg_<xsl:value-of select="$CName"/>)
                    {
                        non_nil_exists = AXIS2_TRUE;
                    }
                    else {
                        if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> != NULL)
                        {
                            size = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                            for(j = 0, non_nil_count = 0; j &lt; size; j ++ )
                            {
                                if(i == j) continue; <!-- should not count the ith element -->
                                if(NULL != axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i))
                                {
                                    non_nil_count ++;
                                    non_nil_exists = AXIS2_TRUE;
                                    if(non_nil_count >= <xsl:value-of select="@minOccurs"/>)
                                    {
                                        break;
                                    }
                                }
                            }

                        <xsl:if test="not(@nillabe) and not(@minOccurs='0')"> <!-- if minOccurs=0 then no need to have error messages -->
                            if( non_nil_count &lt; <xsl:value-of select="@minOccurs"/>)
                            {
                                   AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Size of the array of <xsl:value-of select="$propertyName"/> is beinng set to be smaller than the specificed number of minOccurs(<xsl:value-of select="@minOccurs"/>)");
                                   return AXIS2_FAILURE;
                            }
                        </xsl:if>
                        }
                    }
                  </xsl:otherwise>
                </xsl:choose>

                <xsl:if test ="not(@nillabe) and not(@minOccurs='0')">
                   if(!non_nil_exists)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                       return AXIS2_FAILURE;
                   }
                </xsl:if>

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = axutil_array_list_create(env, 10);
                }
                
                /* check whether there already exist an element */
                element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                if(NULL != element)
                {
                  <!-- Some repeated code -->
                  <!-- For all inside the choose, propertyInstanceName = element -->
                  <xsl:choose>
                     <xsl:when test="@ours">
                        <xsl:value-of select="substring-before(@type, '_t*')"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                        <!-- free ints, int64_ts, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                  </xsl:choose>
                }

                <xsl:if test ="@nillabe or @minOccurs='0'">
                    if(!non_nil_exists)
                    {
                        <!-- No need to worry further -->
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                        axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, NULL);
                        <xsl:if test="$choice">
                            <!-- Should not check the current_choice here, since this is a setting to null -->
                        </xsl:if>
                        return AXIS2_SUCCESS;
                    }
                </xsl:if>
                
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                   <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/> =  (<xsl:value-of select="$PropertyTypeArrayParam"/>)
                            AXIS2_MALLOC(env->allocator, sizeof(<xsl:value-of select="@type"/>));
                   if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for new value of <xsl:value-of select="$propertyName"/>");
                       return AXIS2_FAILURE;
                       
                   }
                   <xsl:text>*ptr_param_</xsl:text><xsl:value-of select="$CName"/> = <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>;
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:when>
                  <xsl:when test="@type='axis2_char_t*'">
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, axutil_strdup(env, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>));
                  </xsl:when>
                  <xsl:otherwise>
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                <xsl:if test="$choice">
                    <xsl:value-of select="$name"/>->current_choice = "<xsl:value-of select="@nsuri"/>:<xsl:value-of select="$propertyName"/>";
                </xsl:if>
                return AXIS2_SUCCESS;
            }

            /**
             * Add to <xsl:value-of select="$propertyName"/>.
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env,
                    <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>)
             {
                <xsl:if test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                   <xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text> ptr_param_</xsl:text><xsl:value-of select="$CName"/>;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                  </xsl:when>
                  <xsl:otherwise>
                    if(NULL == arg_<xsl:value-of select="$CName"/>)
                    {
                      <xsl:choose>
                        <xsl:when test ="not(@nillabe) and not(@minOccurs='0')">
                           AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                           return AXIS2_FAILURE;
                        </xsl:when>
                        <xsl:otherwise>
                           return AXIS2_SUCCESS; <!-- just no need to waist more time -->
                        </xsl:otherwise>
                      </xsl:choose>
                    }
                  </xsl:otherwise>
                </xsl:choose>

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = axutil_array_list_create(env, 10);
                }
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for <xsl:value-of select="$propertyName"/>");
                    return AXIS2_FAILURE;
                    
                }
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                   <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/> =  (<xsl:value-of select="$PropertyTypeArrayParam"/>)
                            AXIS2_MALLOC(env->allocator, sizeof(<xsl:value-of select="@type"/>));
                   if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for new value of <xsl:value-of select="$propertyName"/>");
                       return AXIS2_FAILURE;
                       
                   }
                   <xsl:text>*ptr_param_</xsl:text><xsl:value-of select="$CName"/> = <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>;
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:when>
                  <xsl:when test="@type='axis2_char_t*'">
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, axutil_strdup(env, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>));
                  </xsl:when>
                  <xsl:otherwise>
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                return AXIS2_SUCCESS;
             }

            /**
             * Get the size of the <xsl:value-of select="$propertyName"/> array.
             */
            int AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env)
            {
                AXIS2_ENV_CHECK(env, -1);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, -1);
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    return 0;
                }
                return axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
            }

            /**
             * remove the ith element, same as set_nil_at.
             */
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_remove_<xsl:value-of select="$CName"/>_at(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                    const axutil_env_t *env, int i)
            {
                return <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil_at(<xsl:value-of select="$name"/>, env, i);
            }

           </xsl:if> <!-- closes the isarray -->

           /**
            * resetter for <xsl:value-of select="$propertyName"/>
            */
           axis2_status_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env)
           {
               int i = 0;
               int count = 0;
               void *element = NULL;

               AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
               AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
               

               <xsl:if test="@isarray or @ours or @type='axis2_char_t*' or @type='axutil_qname_t*' or @type='axutil_duration_t*' or @type='axutil_uri_t*' or @type='axutil_date_time_t*' or @type='axutil_base64_binary_t*'">
                <!-- handles arrays -->
                <xsl:if test="@isarray">
                  if (<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> != NULL)
                  {
                      count = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                      for(i = 0; i &lt; count; i ++)
                      {
                         element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                </xsl:if>
            
                <!-- the following element can be inside array or exist independently-->
                if(<xsl:value-of select="$justPropertyInstanceName"/> != NULL)
                {
                   <!-- how to free all the ours things -->
                   <xsl:choose>
                     <xsl:when test="@ours">
                        <xsl:value-of select="substring-before(@type, '_t*')"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                       <xsl:if test="@isarray">
                        <!-- free ints, int64_ts, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                       </xsl:if>
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*'">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                   </xsl:choose>
                   <xsl:value-of select="$justPropertyInstanceName"/> = NULL;
                }
            
                <!--/xsl:if-->
                <!-- close tags arrays -->
                <xsl:if test="@isarray">
                      }
                      axutil_array_list_free(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                  }
                </xsl:if>
               </xsl:if> <!--close for test of primitive types -->
               <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE; 
               return AXIS2_SUCCESS;
           }

           /**
            * Check whether <xsl:value-of select="$propertyName"/> is nill
            */
           axis2_bool_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_is_<xsl:value-of select="$CName"/>_nil(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env)
           {
               AXIS2_ENV_CHECK(env, AXIS2_TRUE);
               AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_TRUE);
               
               return !<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/>;
           }

           /**
            * Set <xsl:value-of select="$propertyName"/> to nill (currently the same as reset)
            */
           axis2_status_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env)
           {
               return <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);
           }

           <xsl:if test="@isarray">
           /**
            * Check whether <xsl:value-of select="$propertyName"/> is nill at i
            */
           axis2_bool_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_is_<xsl:value-of select="$CName"/>_nil_at(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env, int i)
           {
               AXIS2_ENV_CHECK(env, AXIS2_TRUE);
               AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_TRUE);
               
               return (<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> == AXIS2_FALSE ||
                        NULL == <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> || 
                        NULL == axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i));
           }

           /**
            * Set <xsl:value-of select="$propertyName"/> to nill at i
            */
           axis2_status_t AXIS2_CALL
           <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_nil_at(
                   <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                   const axutil_env_t *env, int i)
           {
                void *element = NULL;
                int size = 0;
                int j;
                axis2_bool_t non_nil_exists = AXIS2_FALSE;

                int k = 0;

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL ||
                            <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> == AXIS2_FALSE)
                {
                    <!-- just assume it s null -->
                    non_nil_exists = AXIS2_FALSE;
                }
                else
                {
                    size = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                    for(j = 0, k = 0; j &lt; size; j ++ )
                    {
                        if(i == j) continue; <!-- should not count the ith element -->
                        if(NULL != axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i))
                        {
                            k ++;
                            non_nil_exists = AXIS2_TRUE;
                            if( k >= <xsl:value-of select="@minOccurs"/>)
                            {
                                break;
                            }
                        }
                    }
                }
                <xsl:if test ="not(@nillabe) and not(@minOccurs='0')">
                   if(!non_nil_exists)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "All the elements in the array of <xsl:value-of select="$propertyName"/> is being set to NULL, but it is not a nullable or minOccurs=0 element");
                       return AXIS2_FAILURE;
                   }
                </xsl:if>

                if( k &lt; <xsl:value-of select="@minOccurs"/>)
                {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Size of the array of <xsl:value-of select="$propertyName"/> is beinng set to be smaller than the specificed number of minOccurs(<xsl:value-of select="@minOccurs"/>)");
                       return AXIS2_FAILURE;
                }
 
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                    <!-- just assume it s null -->
                    return AXIS2_SUCCESS;
                }

                /* check whether there already exist an element */
                element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                if(NULL != element)
                {
                  <!-- Some repeated code -->
                  <!-- For all inside the choose, propertyInstanceName = element -->
                  <xsl:choose>
                     <xsl:when test="@ours">
                        <xsl:value-of select="substring-before(@type, '_t*')"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                        <!-- free ints, int64_ts, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                  </xsl:choose>
                }

                <xsl:if test ="@nillabe or @minOccurs='0'">
                    if(!non_nil_exists)
                    {
                        <!-- No need to worry further -->
                        <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                        axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, NULL);
                        return AXIS2_SUCCESS;
                    }
                </xsl:if>

                <!-- for all the other case just set the ith element NULL -->
                axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, NULL);
                
                return AXIS2_SUCCESS;

           }

           </xsl:if> <!-- end of checkiing is array -->
        </xsl:for-each>

        <!-- The section covers the list types, this almost rewrite above setters/getters -->
        <xsl:for-each select="itemtype">
            <xsl:variable name="propertyType">axutil_array_list_t*</xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>
            <xsl:variable name="CName"><xsl:value-of select="$just_name"></xsl:value-of></xsl:variable>

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
            
            <xsl:variable name="propertyInstanceName">(<xsl:value-of select="$nativePropertyType"/>)element</xsl:variable>
            <xsl:variable name="justPropertyInstanceName">element</xsl:variable>
            <xsl:variable name="constValue">
                <xsl:choose>
                   <xsl:when test="@type='axis2_char_t*' or @type='unsigned short' or @type='uint64_t' or @type='unsigned int' or @type='unsigned char' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t'">const </xsl:when>
                </xsl:choose>
            </xsl:variable>
         
        /**
         * Get the ith element of <xsl:value-of select="$propertyName"/>.
         */
        <xsl:value-of select="$nativePropertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i)
        {
                <xsl:value-of select="$PropertyTypeArrayParam"/> ret_val;

                <xsl:choose>
                  <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                    AXIS2_ENV_CHECK(env, (<xsl:value-of select="$nativePropertyType"/>)0);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, (<xsl:value-of select="$nativePropertyType"/>)0);
                  </xsl:when>
                  <xsl:otherwise>
                    AXIS2_ENV_CHECK(env, NULL);
                    AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
                  </xsl:otherwise>
                </xsl:choose>

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    return (<xsl:value-of select="$nativePropertyType"/>)0;
                }
                ret_val = (<xsl:value-of select="$PropertyTypeArrayParam"/>)axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t' or @type='axis2_bool_t'">
                    if(ret_val)
                    {
                        return *ret_val;
                    }
                    return (<xsl:value-of select="$nativePropertyType"/>)0;
                  </xsl:when>
                  <xsl:otherwise>
                    return ret_val;
                  </xsl:otherwise>
                </xsl:choose>
            
        }

        /**
         * Set the ith element of <xsl:value-of select="$propertyName"/>. (If the ith already exist, it will be replaced)
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i,
                <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>)
        {
                void *element = NULL;
                int size = 0;
                int j;

                <xsl:if test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                   <xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text> ptr_param_</xsl:text><xsl:value-of select="$CName"/>;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
                
                if( <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> &amp;&amp;
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> &amp;&amp;
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                    arg_<xsl:value-of select="$CName"/> == *((<xsl:value-of select="$PropertyTypeArrayParam"/>)axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i)))
                  </xsl:when>
                  <xsl:otherwise>
                    arg_<xsl:value-of select="$CName"/> == (<xsl:value-of select="$nativePropertyType"/>)axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i))
                  </xsl:otherwise>
                </xsl:choose>
                {
                    return AXIS2_SUCCESS; 
                }


                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = axutil_array_list_create(env, 10);
                }
                
                /* check whether there already exist an element */
                element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                if(NULL != element)
                {
                  <!-- Some repeated code -->
                  <!-- For all inside the choose, propertyInstanceName = element -->
                  <xsl:choose>
                     <xsl:when test="@ours">
                        <xsl:value-of select="substring-before(@type, '_t*')"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                        <!-- free ints, int64_ts, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                  </xsl:choose>
                }

                
                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                   <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/> =  (<xsl:value-of select="$PropertyTypeArrayParam"/>)
                            AXIS2_MALLOC(env->allocator, sizeof(<xsl:value-of select="@type"/>));
                   if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for new value of <xsl:value-of select="$propertyName"/>");
                       return AXIS2_FAILURE;
                       
                   }
                   <xsl:text>*ptr_param_</xsl:text><xsl:value-of select="$CName"/> = <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>;
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:when>
                  <xsl:when test="@type='axis2_char_t*'">
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, axutil_strdup(env, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>));
                  </xsl:when>
                  <xsl:otherwise>
                   axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                return AXIS2_SUCCESS;

        }

        /**
         * Add to <xsl:value-of select="$propertyName"/>.
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_add_<xsl:value-of select="$CName"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env,
                <xsl:value-of select="$constValue"/><xsl:value-of select="$nativePropertyType"/><xsl:text> arg_</xsl:text> <xsl:value-of select="$CName"/>)
        {
                <xsl:if test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                   <xsl:value-of select="$PropertyTypeArrayParam"/><xsl:text> ptr_param_</xsl:text><xsl:value-of select="$CName"/>;
                </xsl:if>

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);

                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = axutil_array_list_create(env, 10);
                }
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for <xsl:value-of select="$propertyName"/>");
                    return AXIS2_FAILURE;
                }

                <xsl:choose>
                  <xsl:when test="@type='unsigned short' or @type='unsigned char' or @type='unsigned int' or @type='uint64_t' or @type='short' or @type='char' or @type='int' or @type='float' or @type='double' or @type='int64_t' or @type='axis2_bool_t' or @type='axis2_unsigned_byte_t' or @type='axis2_byte_t'">
                   <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/> =  (<xsl:value-of select="$PropertyTypeArrayParam"/>)
                            AXIS2_MALLOC(env->allocator, sizeof(<xsl:value-of select="@type"/>));
                   if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                   {
                       AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI, "Failed in allocatting memory for new value of <xsl:value-of select="$propertyName"/>");
                       return AXIS2_FAILURE;
                       
                   }
                   <xsl:text>*ptr_param_</xsl:text><xsl:value-of select="$CName"/> = <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>;
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, <xsl:text>ptr_param_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:when>
                  <xsl:when test="@type='axis2_char_t*'">
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, axutil_strdup(env, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>));
                  </xsl:when>
                  <xsl:otherwise>
                   axutil_array_list_add(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, <xsl:text>arg_</xsl:text><xsl:value-of select="$CName"/>);
                  </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;
                return AXIS2_SUCCESS;

        }

        /**
         * Get the size of the <xsl:value-of select="$propertyName"/> array.
         */
        int AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_sizeof_<xsl:value-of select="$CName"/>(
                    <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>, 
                    const axutil_env_t *env)
        {
                AXIS2_ENV_CHECK(env, -1);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, -1);
                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    return 0;
                }
                return axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
        }


        /**
         * Remove the ith element of <xsl:value-of select="$propertyName"/>.
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_remove_<xsl:value-of select="$CName"/>_at(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, int i)
        {
                void *element = NULL;
                int size = 0;
                int j;

                int k = 0;

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);


                if(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> == NULL)
                {
                    <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE;
                    <!-- just assume it s null -->
                    return AXIS2_SUCCESS;
                }

                /* check whether there already exist an element */
                element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
                if(NULL != element)
                {
                  <!-- Some repeated code -->
                  <!-- For all inside the choose, propertyInstanceName = element -->
                  <xsl:choose>
                     <xsl:when test="@ours">
                        <xsl:value-of select="substring-before(@type, '_t*')"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                        <!-- free ints, int64_ts, float in array-->
                        /* we keep primtives as pointers in arrasy, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*' and not(@isarray)">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                  </xsl:choose>
                }

                <!-- for all the other case just set the ith element NULL -->
                axutil_array_list_set(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> , env, i, NULL);
                
                return AXIS2_SUCCESS;
        }

        /**
         * Getter for <xsl:value-of select="$propertyName"/> by  Property Number <xsl:value-of select="position()"/>
         */
        <xsl:value-of select="$propertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_property<xsl:value-of select="position()"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env)
        {
            return <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>,
                                                        env);
        }


        /**
         * Getter for <xsl:value-of select="$propertyName"/>.
         */
        <xsl:value-of select="$propertyType"/> AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env)
        {
             <xsl:choose>
               <xsl:when test="$propertyType='unsigned short' or $propertyType='unsigned char' or $propertyType='unsigned int' or $propertyType='uint64_t' or $propertyType='short' or $propertyType='axis2_unsigned_byte_t' or $propertyType='axis2_byte_t' or $propertyType='axis2_bool_t' or $propertyType='char' or $propertyType='int' or $propertyType='float' or $propertyType='double' or $propertyType='int64_t'">
                 AXIS2_ENV_CHECK(env, (<xsl:value-of select="$propertyType"/>)0);
                 AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, (<xsl:value-of select="$propertyType"/>)0);
               </xsl:when>
               <xsl:otherwise>
                 AXIS2_ENV_CHECK(env, NULL);
                 AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, NULL);
               </xsl:otherwise>
             </xsl:choose>

             return <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>;
        }

        /**
         * Setter for <xsl:value-of select="$propertyName"/>.
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env,
            <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text> arg_<xsl:value-of select="$CName"/>)
        {

                AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
                AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
                
                if(<xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> &amp;&amp;
                        arg_<xsl:value-of select="$CName"/> == <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>)
                {
                    <xsl:if test="$choice">
                        <xsl:value-of select="$name"/>->current_choice = "<xsl:value-of select="@nsuri"/>:<xsl:value-of select="$propertyName"/>";
                    </xsl:if>
                    return AXIS2_SUCCESS; 
                }

                <!-- first reset whatever already in there -->
                <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(<xsl:value-of select="$name"/>, env);

                <xsl:if test="(@ours or not($propertyType='unsigned char' or $propertyType='unsigned int' or $propertyType='unsigned short' or $propertyType='uint64_t' or $propertyType='char' or $propertyType='int' or $propertyType='short' or $propertyType='float' or $propertyType='axis2_unsigned_byte_t' or  $propertyType='axis2_byte_t' or $propertyType='double' or $propertyType='int64_t' or $propertyType='axis2_bool_t'))">
                if(NULL == arg_<xsl:value-of select="$CName"/>)
                {
                    /* We are already done */
                    return AXIS2_SUCCESS;
                }
                </xsl:if>

                <xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> = arg_<xsl:value-of select="$CName"/>;
                <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_TRUE;

                return AXIS2_SUCCESS;
            
        }
        

        /**
         * Resetter for <xsl:value-of select="$propertyName"/>
         */
        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_reset_<xsl:value-of select="$CName"/>(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env)
        {
               int i = 0;
               int count = 0;
               void *element = NULL;

               AXIS2_ENV_CHECK(env, AXIS2_FAILURE);
               AXIS2_PARAM_CHECK(env->error, <xsl:value-of select="$name"/>, AXIS2_FAILURE);
               

                <!-- This is always an array -->
                  if (<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/> != NULL)
                  {
                      count = axutil_array_list_size(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                      for(i = 0; i &lt; count; i ++)
                      {
                         element = axutil_array_list_get(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env, i);
            
                <!-- the following element can be inside array or exist independently-->
                if(<xsl:value-of select="$justPropertyInstanceName"/> != NULL)
                {
                   <!-- how to free all the ours things -->
                   <xsl:choose>
                     <xsl:when test="@ours">
                        <xsl:value-of select="substring-before(@type, '_t*')"/>_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='unsigned short' or $nativePropertyType='unsigned char' or $nativePropertyType='unsigned int' or $nativePropertyType='uint64_t' or $nativePropertyType='short' or $nativePropertyType='axis2_unsigned_byte_t' or $nativePropertyType='axis2_byte_t' or $nativePropertyType='axis2_bool_t' or $nativePropertyType='char' or $nativePropertyType='int' or $nativePropertyType='float' or $nativePropertyType='double' or $nativePropertyType='int64_t'">
                        <!-- free ints, int64_ts, float in array-->
                        /* we keep primtives as pointers in arrays, so need to free them */
                        AXIS2_FREE(env-> allocator, element);
                     </xsl:when>
            
                     <!-- free axis2_char_t s -->
                     <xsl:when test="$nativePropertyType='axis2_char_t*'">
                        AXIS2_FREE(env-> allocator, <xsl:value-of select="$propertyInstanceName"/>);
                     </xsl:when>
            
                     <!-- free nodes -->
                     <xsl:when test="$nativePropertyType='axiom_node_t*'">
                      axiom_node_free_tree (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_qname_t*'">
                      axutil_qname_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_uri_t*'">
                      axutil_uri_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_duration_t*'">
                      axutil_duration_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$nativePropertyType='axutil_date_time_t*'">
                      axutil_date_time_free(<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <xsl:when test="$propertyType='axutil_base64_binary_t*'">
                      axutil_base64_binary_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
                       <xsl:when test="$propertyType='axutil_duration_t*'">
                      axutil_duration_free (<xsl:value-of select="$propertyInstanceName"/>, env);
                     </xsl:when>
            
                     <!--TODO: This should be extended for all the types that should be freed.. -->
                     <xsl:otherwise>
                       /* This is an unknown type or a primitive. Please free this manually*/
                     </xsl:otherwise>
                   </xsl:choose>
                   <xsl:value-of select="$justPropertyInstanceName"/> = NULL;
                }
            
                <!--/xsl:if-->
                <!-- close tags arrays -->
                      }
                      axutil_array_list_free(<xsl:value-of select="$name"/>->property_<xsl:value-of select="$CName"/>, env);
                  }
               <xsl:value-of select="$name"/>->is_valid_<xsl:value-of select="$CName"/> = AXIS2_FALSE; 
               return AXIS2_SUCCESS;

        }


        </xsl:for-each>

        <xsl:for-each select="memberType">
            <xsl:variable name="member_type" select="@type"/>
            <xsl:variable name="member_name"><xsl:text></xsl:text><xsl:value-of select="@originalName"/></xsl:variable>
 
            <xsl:value-of select="$member_type"/> AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_get_<xsl:value-of select="$member_name"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env)
            {
                if(!axutil_strcmp(<xsl:value-of select="$name"/>->current_value, "<xsl:value-of select="@originalName"/>"))
                {
                    return <xsl:value-of select="$name"/>->member_type._<xsl:value-of select="$member_name"/>;
                }
                return (<xsl:value-of select="$member_type"/>)0;
            }
 
            axis2_status_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_set_<xsl:value-of select="$member_name"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env, 
                <xsl:value-of select="$member_type"/> member_type)
            {
                axis2_status_t status;
                status = <xsl:value-of select="$axis2_name"/>_reset_members(<xsl:value-of select="$name"/>, env);
               
                if(status == AXIS2_SUCCESS)
                {
                    <xsl:choose>
                    <xsl:when test="$member_type='axis2_char_t*'">
                        <xsl:value-of select="$name"/>->member_type._<xsl:value-of select="$member_name"/> = axutil_strdup(env, member_type);
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$name"/>->member_type._<xsl:value-of select="$member_name"/> = member_type;
                    </xsl:otherwise>
                    </xsl:choose>
                    <xsl:value-of select="$name"/>->current_value = "<xsl:value-of select="@originalName"/>";
                }

                return status;
            }

            axis2_bool_t AXIS2_CALL
            <xsl:value-of select="$axis2_name"/>_is_valid_<xsl:value-of select="$member_name"/>(
                <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
                const axutil_env_t *env)
            {
                return !axutil_strcmp(<xsl:value-of select="$name"/>->current_value, "<xsl:value-of select="@originalName"/>");
            }
        </xsl:for-each>
    
        <xsl:if test="$isUnion">

        axis2_status_t AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_reset_members(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env)
        {

            if(!axutil_strcmp(<xsl:value-of select="$name"/>->current_value, ""))
            {
                /* do nothing */
            }
            <xsl:for-each select="memberType">
                <xsl:variable name="member_type" select="@type"/>
                <xsl:variable name="member_name"><xsl:text></xsl:text><xsl:value-of select="@originalName"/></xsl:variable>

                else if(!axutil_strcmp(<xsl:value-of select="$name"/>->current_value, "<xsl:value-of select="@originalName"/>"))
                {
                    <xsl:choose>
                        <xsl:when test="@ours">
                            <xsl:value-of select="substring-before(@type, '_t*')"/>_free(<xsl:value-of select="$name"/>->member_type._<xsl:value-of select="$member_name"/>, env);
                        </xsl:when>
                        <xsl:when test="$member_type='axis2_char_t*'">
                            AXIS2_FREE(env->allocator, <xsl:value-of select="$name"/>->member_type._<xsl:value-of select="$member_name"/>);
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- nothing to free inside here -->
                        </xsl:otherwise>
                    </xsl:choose>
                }
            </xsl:for-each>   
            <xsl:value-of select="$name"/>->current_value = "";
            return AXIS2_SUCCESS;
        }

        axis2_char_t* AXIS2_CALL
        <xsl:value-of select="$axis2_name"/>_current_member_type(
            <xsl:value-of select="$axis2_name"/>_t*<xsl:text> </xsl:text><xsl:value-of select="$name"/>,
            const axutil_env_t *env)
        {
            return <xsl:value-of select="$name"/>->current_value;
        }
        </xsl:if>

    </xsl:template>

    <xsl:template match="mapper">
        <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
        <xsl:variable name="axis2_name">axis2_<xsl:value-of select="@name"/></xsl:variable>

        /**
         * <xsl:value-of select="$axis2_name"/>.c
         *
         * This file was auto-generated from WSDL
         * by the Apache Axis2/Java version: #axisVersion# #today#
         */

        #include "<xsl:value-of select="$axis2_name"/>.h"

        <xsl:for-each select="type">#include "adb_<xsl:value-of select="@shortname"/>.h"
        </xsl:for-each>

        struct adb_type
        {
            axis2_char_t *property_Type;
        };

        /**
         * Auxiliary function to determine an ADB object type from its Axiom node.
         * @param env pointer to environment struct
         * @param node double pointer to the parent node to deserialize
         * @return type name on success, else NULL
         */
        axis2_char_t *AXIS2_CALL
        axis2_extension_mapper_type_from_node(
            const axutil_env_t *env,
            axiom_node_t** node)
        {
            axiom_node_t *parent = *node;
            axutil_qname_t *element_qname = NULL;
            axiom_element_t *element = NULL;

            axutil_hash_index_t *hi;
            void *val;
            axiom_attribute_t *type_attr;
            axutil_hash_t *ht;
            axis2_char_t *temp;
            axis2_char_t *type;

            while(parent &amp;&amp; axiom_node_get_node_type(parent, env) != AXIOM_ELEMENT)
            {
                parent = axiom_node_get_next_sibling(parent, env);
            }

            if (NULL == parent)
            {
                /* This should be checked before everything */
                AXIS2_LOG_ERROR(env->log, AXIS2_LOG_SI,
                            "Failed in building adb object : "
                            "NULL elemenet can not be passed to deserialize");
                return AXIS2_FAILURE;
            }

            element = (axiom_element_t *)axiom_node_get_data_element(parent, env);

            ht = axiom_element_get_all_attributes(element, env);

            if (ht == NULL)
                return NULL;

            for (hi = axutil_hash_first(ht, env); hi; hi = axutil_hash_next(env, hi)) {
                axis2_char_t *localpart;
                axutil_hash_this(hi, NULL, NULL, &amp;val);
                type_attr = (axiom_attribute_t *)val;
                localpart = axutil_qname_get_localpart(axiom_attribute_get_qname(type_attr, env), env);
                if (axutil_strcmp(localpart, "type") == 0) break;
            }

            type = axiom_attribute_get_value(type_attr, env);
            if (type != NULL &amp;&amp; (temp = axutil_strchr(type, ':')) != NULL)
            {
                if (axutil_strchr(temp, ':') != NULL)
                    type = temp + 1; /* Pointer arithmetic */
            }

            return type;
        }

        axis2_char_t* AXIS2_CALL
        adb_type_get_type(const adb_type_t *object)
        {
            if (object != NULL)
              return object->property_Type;

            return NULL;
        }

        adb_type_t* AXIS2_CALL
        axis2_extension_mapper_create_from_node(
            const axutil_env_t *env,
            axiom_node_t** node,
            axis2_char_t *default_type)
        {
            axis2_char_t *type = axis2_extension_mapper_type_from_node(env, node);

            if (type != NULL)
            {
              <xsl:for-each select="type">
              if (axutil_strcmp(type, "<xsl:value-of select="@shortname"/>") == 0)
              {
                  return (adb_type_t*) adb_<xsl:value-of select="@shortname"/>_create(env);
              }
              </xsl:for-each>
            }

            <xsl:for-each select="type">
            if (axutil_strcmp(default_type, "adb_<xsl:value-of select="@shortname"/>") == 0)
            {
                return (adb_type_t*) adb_<xsl:value-of select="@shortname"/>_create(env);
            }
            </xsl:for-each>

            return NULL;
        }

        axis2_status_t AXIS2_CALL
        axis2_extension_mapper_free(
            adb_type_t* _object,
            const axutil_env_t *env,
            axis2_char_t *default_type)
        {
            if (_object != NULL &amp;&amp; adb_type_get_type(_object) != NULL)
            {
            <xsl:for-each select="type">
                if (axutil_strcmp(adb_type_get_type(_object), "adb_<xsl:value-of select="@shortname"/>") == 0)
                {
                    return adb_<xsl:value-of select="@shortname"/>_free_obj(
                    (<xsl:value-of select="@classname"/>) _object, env);
                }
            </xsl:for-each>
            }

            <xsl:for-each select="type">
            if (axutil_strcmp(default_type, "adb_<xsl:value-of select="@shortname"/>") == 0)
            {
                return adb_<xsl:value-of select="@shortname"/>_free_obj(
                (<xsl:value-of select="@classname"/>) _object, env);
            }
            </xsl:for-each>

            return AXIS2_FAILURE;
        }

        axis2_status_t AXIS2_CALL
        axis2_extension_mapper_deserialize(
            adb_type_t* _object,
            const axutil_env_t *env,
            axiom_node_t** dp_parent,
            axis2_bool_t *dp_is_early_node_valid,
            axis2_bool_t dont_care_minoccurs,
            axis2_char_t *default_type)
        {
            if (_object != NULL &amp;&amp; adb_type_get_type(_object) != NULL)
            {
            <xsl:for-each select="type">
                if (axutil_strcmp(adb_type_get_type(_object), "adb_<xsl:value-of select="@shortname"/>") == 0)
                {
                    return adb_<xsl:value-of select="@shortname"/>_deserialize_obj(
                    (<xsl:value-of select="@classname"/>) _object, env, dp_parent, dp_is_early_node_valid, dont_care_minoccurs);
                }
            </xsl:for-each>
            }

            <xsl:for-each select="type">
            if (axutil_strcmp(default_type, "adb_<xsl:value-of select="@shortname"/>") == 0)
            {
                return adb_<xsl:value-of select="@shortname"/>_deserialize_obj(
                (<xsl:value-of select="@classname"/>) _object, env, dp_parent, dp_is_early_node_valid, dont_care_minoccurs);
            }
            </xsl:for-each>

            return AXIS2_FAILURE;
        }

        axiom_node_t* AXIS2_CALL
        axis2_extension_mapper_serialize(
            adb_type_t* _object,
            const axutil_env_t *env,
            axiom_node_t* om_node,
            axiom_element_t *om_element,
            int tag_closed,
            axutil_hash_t *namespaces,
            int *next_ns_index,
            axis2_char_t *default_type)
        {
            if (_object != NULL &amp;&amp; adb_type_get_type(_object) != NULL)
            {
                <xsl:for-each select="type">
                if (axutil_strcmp(adb_type_get_type(_object), "adb_<xsl:value-of select="@shortname"/>") == 0)
                {
                    return adb_<xsl:value-of select="@shortname"/>_serialize_obj(
                    (<xsl:value-of select="@classname"/>) _object, env, om_node, om_element, tag_closed, namespaces, next_ns_index);
                }
            </xsl:for-each>
            }

            <xsl:for-each select="type">
            if (axutil_strcmp(default_type, "adb_<xsl:value-of select="@shortname"/>") == 0)
            {
                return adb_<xsl:value-of select="@shortname"/>_serialize_obj(
                (<xsl:value-of select="@classname"/>) _object, env, om_node, om_element, tag_closed, namespaces, next_ns_index);
            }
            </xsl:for-each>

            return AXIS2_FAILURE;
        }
    </xsl:template>

</xsl:stylesheet>
