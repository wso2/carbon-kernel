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
    <xsl:template match="bean[not(@helperMode)]">

        <xsl:variable name="name" select="@name"/>
        <xsl:variable name="choice" select="@choice"/>
        <xsl:variable name="simple" select="@simple"/>
        <xsl:variable name="ordered" select="@ordered"/>
        <xsl:variable name="unordered" select="not($ordered)"/>  <!-- for convenience -->
        <xsl:variable name="isType" select="@type"/>
        <xsl:variable name="anon" select="@anon"/>
        <xsl:variable name="union" select="@union"/>
        <xsl:variable name="list" select="@list"/>

        <xsl:variable name="nsuri" select="@nsuri"/>
        <xsl:variable name="originalName" select="@originalName"/>
        <xsl:variable name="nsprefix" select="@nsprefix"/>
        <xsl:variable name="extension" select="@extension"/>
        <xsl:variable name="restriction" select="@restriction"/>
        <xsl:variable name="mapperClass" select="@mapperClass"/>
        <xsl:variable name="particleClass" select="@particleClass"/>
        <xsl:variable name="hasParticleType" select="@hasParticleType"/>
        <xsl:variable name="usewrapperclasses" select="@usewrapperclasses"/>

        <!-- write the class header. this should be done only when unwrapped -->

        <xsl:if test="not(not(@unwrapped) or (@skip-write))">
/**
 * <xsl:value-of select="$name"/>.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: #axisVersion# #today#
 */

            <xsl:if test="string-length(normalize-space(@package)) > 0">
                package <xsl:value-of select="@package"/>;
            </xsl:if>

            /**
            *  <xsl:value-of select="$name"/> bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        </xsl:if>
        public <xsl:if test="not(@unwrapped) or (@skip-write)">static</xsl:if> <xsl:if test="@isAbstract and @unwrapped and not(@skip-write)">abstract</xsl:if> class <xsl:value-of select="$name"/> <xsl:if test="$extension"> extends <xsl:value-of select="$extension"/></xsl:if> <xsl:if test="$restriction"> extends <xsl:value-of select="$restriction"/></xsl:if>
        <xsl:if test="$union and not($restriction) and not($extension)"> extends  org.apache.axis2.databinding.types.Union </xsl:if>
        implements org.apache.axis2.databinding.ADBBean{
        <xsl:choose>
            <xsl:when test="@type">/* This type was generated from the piece of schema that had
                name = <xsl:value-of select="$originalName"/>
                Namespace URI = <xsl:value-of select="$nsuri"/>
                Namespace Prefix = <xsl:value-of select="$nsprefix"/>
                */
            </xsl:when>
            <xsl:otherwise>
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "<xsl:value-of select="$nsuri"/>",
                "<xsl:value-of select="$originalName"/>",
                "<xsl:value-of select="$nsprefix"/>");

            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="$choice">
            /** Whenever a new property is set ensure all others are unset
             *  There can be only one choice and the last one wins
             */
            private void clearAllSettingTrackers() {
            <xsl:for-each select="property">
                <xsl:if test="not(@attribute)">
                   local<xsl:value-of select="@javaname"/>Tracker = false;
                </xsl:if>
           </xsl:for-each>
            }
        </xsl:if>


        <xsl:for-each select="property">
            <!-- Write only the NOT inherited properties
            but we have to generate two specital methods for simple type restriction and extenesions -->
            <xsl:if test="not(@inherited) or ($simple and $extension) or ($simple and $restriction)">

            <xsl:variable name="propertyType"><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
            <xsl:variable name="propertyName"><xsl:value-of select="@name"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>

            <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
            <xsl:variable name="varName">local<xsl:value-of select="$javaName"/></xsl:variable>
            <xsl:variable name="settingTracker">local<xsl:value-of select="$javaName"/>Tracker</xsl:variable>


            <xsl:variable name="lenFacet"><xsl:value-of select="@lenFacet"/></xsl:variable>
               <xsl:variable name="maxLenFacet"><xsl:value-of select="@maxLenFacet"/></xsl:variable>
               <xsl:variable name="minLenFacet"><xsl:value-of select="@minLenFacet"/></xsl:variable>
               <xsl:variable name="maxExFacet"><xsl:value-of select="@maxExFacet"/></xsl:variable>
               <xsl:variable name="minExFacet"><xsl:value-of select="@minExFacet"/></xsl:variable>
               <xsl:variable name="maxInFacet"><xsl:value-of select="@maxInFacet"/></xsl:variable>
               <xsl:variable name="minInFacet"><xsl:value-of select="@minInFacet"/></xsl:variable>
               <xsl:variable name="patternFacet"><xsl:value-of select="@patternFacet"/></xsl:variable>
            <xsl:variable name="shortTypeNameUncapped"  select="@shorttypename"/>
            <xsl:variable name="shortTypeName"
               select="concat(translate( substring($shortTypeNameUncapped, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($shortTypeNameUncapped, 2, string-length($shortTypeNameUncapped)))" />


            <xsl:choose>
                <xsl:when test="@removed">

               /**
               * Auto generated getter method
               * Overridden from <xsl:value-of select="$restriction"/>
               *
               * @throws RuntimeException
               */
               public  <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>get<xsl:value-of select="$javaName"/>(){
                   throw new java.lang.RuntimeException("The property has been removed by restriction");
               }

               /**
               * Auto generated setter method
               * Overridden from <xsl:value-of select="$restriction"/>
               *
               * @param param <xsl:value-of select="$javaName"/>
               * @throws RuntimeException
               */
               public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                   throw new java.lang.RuntimeException("The property has been removed by restriction");
               }

               </xsl:when>
                <xsl:otherwise>
                   <xsl:choose>
                       <xsl:when test="(@restricted) and (@occuranceChanged) and (not(@typeChanged)) and (not(@rewrite))">
                            <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>


                         <!-- generate the validator Method, this is specifiacally for validating the arrays-->
                         /**
                         * Validate the array for <xsl:value-of select="$javaName"/>
                         * Overridden from <xsl:value-of select="$restriction"/>
                         */
                         protected void validate<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                         <xsl:if test="not(@unbound) and @array">
                              if ((param != null) &amp;&amp; (param.length &gt; <xsl:value-of select="@maxOccurs"/>)){
                                throw new java.lang.RuntimeException();
                              }
                         </xsl:if>
                         <xsl:if test="$min!=0 and @array">
                              if ((param != null) &amp;&amp; (param.length &lt; <xsl:value-of select="$min"/>)){
                                throw new java.lang.RuntimeException();
                              }
                         </xsl:if>
                         }


                         /**
                         * Auto generated setter method
                         * Overridden from <xsl:value-of select="$restriction"/>
                         *
                         * @param param <xsl:value-of select="$javaName"/>
                         */
                         public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                         <!-- call the validator-->
                                validate<xsl:value-of select="$javaName"/>(param);

                         <xsl:if test="$choice">
                              clearAllSettingTrackers();
                         </xsl:if>
                         <xsl:if test="$min=0 or $choice">
                            <xsl:choose>
                               <!--
                                   * the updating of setting tracker for null values should
                                     happen if the attribute is marked as nillable. Else
                                     the user can set a null value and it is never marked
                                     as set
                                   * setting primitive variable always to true
                               -->
                               <xsl:when test="(@primitive and not(@array)) or @nillable">
                                   <xsl:value-of select="$settingTracker"/> = true;
                               </xsl:when>
                               <xsl:otherwise>
                                   <xsl:value-of select="$settingTracker"/> = param != null;
                               </xsl:otherwise>
                            </xsl:choose>
                         </xsl:if>
                              this.<xsl:value-of select="$varName"/>=param;
                         }
                      </xsl:when>
                      <xsl:otherwise>

                        /**
                        * field for <xsl:value-of select="$javaName"/>
                        <xsl:if test="@attribute">
                        * This was an Attribute!</xsl:if>
                        <xsl:if test="@array">
                        * This was an Array!</xsl:if>
                        <xsl:if test="@typeChanged">
                        * Type of this field is a subtype of its original.</xsl:if>
                        <xsl:if test="(@rewrite) and (@occuranceChanged)">
                        * This field was an array in <xsl:value-of select="$restriction"/>.</xsl:if>
                        */

                        <xsl:if test="not(@inherited)">
                            <xsl:choose>
                                <xsl:when test="@defaultValue">
                                    protected <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text><xsl:value-of select="$varName" /> =
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>("<xsl:value-of
                                        select="@defaultValue"/>");
                                </xsl:when>
                                <xsl:otherwise>
                                    protected <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text><xsl:value-of select="$varName" /> ;
                                </xsl:otherwise>
                            </xsl:choose>

                        </xsl:if>
                        <xsl:if test="enumFacet">
                            private static java.util.HashMap _table_ = new java.util.HashMap();

                            // Constructor
                            <xsl:if test="not(@inherited)">
                                protected <xsl:value-of select="$name"/>(<xsl:value-of select="$propertyType"/> value, boolean isRegisterValue) {
                                    <xsl:value-of select="$varName" /> = value;
                                    if (isRegisterValue){
                                        <xsl:choose>
                                           <xsl:when test="@primitive">
                                             _table_.put(<xsl:value-of select="$varName" /> + "", this);
                                           </xsl:when>
                                           <xsl:otherwise>
                                               _table_.put(<xsl:value-of select="$varName" />, this);
                                           </xsl:otherwise>
                                        </xsl:choose>
                                    }

                                }
                            </xsl:if>
                            <xsl:if test="@inherited">
                                protected <xsl:value-of select="$name"/>(<xsl:value-of select="$propertyType"/> value, boolean isRegisterValue) {
                                    super(value,false);
                                    if (isRegisterValue){
                                        <xsl:choose>
                                           <xsl:when test="@primitive">
                                             _table_.put(<xsl:value-of select="$varName" /> + "", this);
                                           </xsl:when>
                                           <xsl:otherwise>
                                               _table_.put(<xsl:value-of select="$varName" />, this);
                                           </xsl:otherwise>
                                        </xsl:choose>
                                    }
                                }
                            </xsl:if>
                            <xsl:if test="not(@inherited)">
                                <xsl:for-each select="enumFacet">
                                    public static final <xsl:value-of select="$propertyType"/> _<xsl:value-of select="@id"/> =
                                        org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>("<xsl:value-of select="@value"/>");
                                </xsl:for-each>
                            </xsl:if>

                            <xsl:for-each select="enumFacet">
                                public static final <xsl:value-of select="$name"/><xsl:text> </xsl:text><xsl:value-of select="@id"/> =
                                    new <xsl:value-of select="$name"/>(_<xsl:value-of select="@id"/>,true);
                            </xsl:for-each>

                                public <xsl:value-of select="$propertyType"/> getValue() { return <xsl:value-of select="$varName" />;}

                                public boolean equals(java.lang.Object obj) {return (obj == this);}
                                public int hashCode() { return toString().hashCode();}
                                public java.lang.String toString() {
                                <xsl:choose>
                                    <xsl:when test="(@primitive) or ($shortTypeName = 'DateTime') or ($shortTypeName = 'Date') or ($shortTypeName = 'Time')">
                                        return org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>);
                                    </xsl:when>
                                    <xsl:otherwise>
                                        return <xsl:value-of select="$varName"/>.toString();
                                    </xsl:otherwise>
                                </xsl:choose>

                                }

                        </xsl:if>
                        <xsl:if test="not(enumFacet)">
                        <!-- Generate a tracker only if the min occurs is zero, which means if the user does
                           not bother to set that value, we do not send it -->
                           <xsl:if test="$min=0 or $choice">
                           /*  This tracker boolean wil be used to detect whether the user called the set method
                          *   for this attribute. It will be used to determine whether to include this field
                           *   in the serialized XML
                           */
                           protected boolean <xsl:value-of select="$settingTracker"/> = false ;

                           public boolean is<xsl:value-of select="$javaName"/>Specified(){
                               return <xsl:value-of select="$settingTracker"/>;
                           }

                           </xsl:if>

                           /**
                           * Auto generated getter method
                           * @return <xsl:value-of select="$propertyType"/>
                           */
                           public  <xsl:value-of select="$propertyType"/><xsl:text> </xsl:text>get<xsl:value-of select="$javaName"/>(){
                               return <xsl:value-of select="$varName"/>;
                           }

                           <!-- When generating the setters, we have to cater differently for the array!-->
                        <xsl:choose>
                               <xsl:when test="@array">
                               <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>


                               <!-- generate the validator Method, this is specifiacally for validating the arrays-->
                              /**
                               * validate the array for <xsl:value-of select="$javaName"/>
                               */
                              protected void validate<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                             <xsl:if test="not(@unbound)">
                              if ((param != null) &amp;&amp; (param.length &gt; <xsl:value-of select="@maxOccurs"/>)){
                                throw new java.lang.RuntimeException();
                              }
                              </xsl:if>
                              <xsl:if test="$min!=0">
                              if ((param != null) &amp;&amp; (param.length &lt; <xsl:value-of select="$min"/>)){
                                throw new java.lang.RuntimeException();
                              }
                              </xsl:if>
                              }


                             /**
                              * Auto generated setter method
                              * @param param <xsl:value-of select="$javaName"/>
                              */
                              public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                              <!-- call the validator-->
                                   validate<xsl:value-of select="$javaName"/>(param);

                               <xsl:if test="$choice">
                                   clearAllSettingTrackers();
                               </xsl:if>
                               <xsl:if test="$min=0 or $choice">
                                  <xsl:choose>
                                       <!--
                                           * the updating of setting tracker for null values should
                                             happen if the attribute is marked as nillable. Else
                                             the user can set a null value and it is never marked
                                             as set
                                           * setting the primitive attribute to true
                                       -->
                                      <xsl:when test="(@primitive and not(@array)) or @nillable">
                                          <xsl:value-of select="$settingTracker"/> = true;
                                      </xsl:when>
                                      <xsl:otherwise>
                                          <xsl:value-of select="$settingTracker"/> = param != null;
                                      </xsl:otherwise>
                                   </xsl:choose>

                               </xsl:if>
                                      this.<xsl:value-of select="$varName"/>=param;
                              }

                               <!--

                               we special case the 'array' scenario and generate a
                               convenience method for adding elements one by one to
                               the array. The current implementation is somewhat
                               inefficient but gets the job done.Since a primitive
                               cannot be treated as an object it has to be ignored!

                             -->
                             <xsl:if test="not(@primitive)">
                             /**
                             * Auto generated add method for the array for convenience
                             * @param param <xsl:value-of select="$basePropertyType"/>
                             */
                             public void add<xsl:value-of select="$javaName"/>(<xsl:value-of select="$basePropertyType"/> param){
                                   if (<xsl:value-of select="$varName"/> == null){
                                   <xsl:value-of select="$varName"/> = new <xsl:value-of select="$propertyType"/>{};
                                   }

                            <xsl:if test="$choice">
                                   clearAllSettingTrackers();
                            </xsl:if>
                            <xsl:if test="$min=0 or $choice">
                                 //update the setting tracker
                                <xsl:value-of select="$settingTracker"/> = true;
                            </xsl:if>

                               java.util.List list =
                            org.apache.axis2.databinding.utils.ConverterUtil.toList(<xsl:value-of select="$varName"/>);
                               list.add(param);
                               this.<xsl:value-of select="$varName"/> =
                             (<xsl:value-of select="$propertyType"/>)list.toArray(
                            new <xsl:value-of select="$basePropertyType"/>[list.size()]);

                             }
                             </xsl:if>
                             <!-- end of special casing for the array-->

                               </xsl:when>
                            <!-- Non array setter method-->
                            <xsl:otherwise>
                            /**
                               * Auto generated setter method
                               * @param param <xsl:value-of select="$javaName"/>
                               */
                               public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/> param){
                            <xsl:if test="$choice">
                                clearAllSettingTrackers();
                            </xsl:if>
                            <xsl:if test="$min=0 or $choice">
                                <xsl:choose>
                                   <xsl:when test="@nillable">
                                       <xsl:value-of select="$settingTracker"/> = true;
                                   </xsl:when>
                                   <xsl:when test="@primitive and not(@array)">
                                       // setting primitive attribute tracker to true
                                       <xsl:value-of select="$settingTracker"/> =
                                       <xsl:choose>
                                           <xsl:when test="$usewrapperclasses">true</xsl:when>
                                           <xsl:when test="$propertyType='int'">param != java.lang.Integer.MIN_VALUE</xsl:when>
                                           <xsl:when test="$propertyType='long'">param != java.lang.Long.MIN_VALUE</xsl:when>
                                           <xsl:when test="$propertyType='byte'">param != java.lang.Byte.MIN_VALUE</xsl:when>
                                           <xsl:when test="$propertyType='double'">!java.lang.Double.isNaN(param)</xsl:when>
                                           <xsl:when test="$propertyType='float'">!java.lang.Float.isNaN(param)</xsl:when>
                                           <xsl:when test="$propertyType='short'">param != java.lang.Short.MIN_VALUE</xsl:when>
                                           <xsl:otherwise>true</xsl:otherwise>
                                       </xsl:choose>;
                                   </xsl:when>
                                   <xsl:otherwise>
                                       <xsl:value-of select="$settingTracker"/> = param != null;
                                   </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>

                               <xsl:choose>
                                    <xsl:when test="(@restrictionBaseType)">
                                       <xsl:choose>
                                        <xsl:when test="(@patternFacet)">
                                            if (org.apache.axis2.databinding.utils.ConverterUtil.convertToString(param).matches("<xsl:value-of select="$patternFacet"/>")) {
                                                this.<xsl:value-of select="$varName"/>=param;
                                            }
                                            else {
                                                throw new java.lang.RuntimeException();
                                            }
                                        </xsl:when>
                                        <xsl:when test="(@lenFacet)">
                                            if (org.apache.axis2.databinding.utils.ConverterUtil.convertToString(param).length() == <xsl:value-of select="@lenFacet"/> ) {
                                                this.<xsl:value-of select="$varName"/>=param;
                                            }
                                            else {
                                                throw new java.lang.RuntimeException();
                                            }
                                        </xsl:when>
                                        <xsl:when test="(@maxLenFacet) or (@minLenFacet)">
                                            if ( <xsl:if test="(@minLenFacet)"> (<xsl:value-of select="$minLenFacet"/> &lt;= java.lang.String.valueOf(param).length())</xsl:if>
                                              <xsl:if test="(@maxLenFacet)"> <xsl:if test="(@minLenFacet)"> &amp;&amp; </xsl:if> (java.lang.String.valueOf(param).length() &lt;= <xsl:value-of select="$maxLenFacet"/>) </xsl:if> ) {
                                                this.<xsl:value-of select="$varName"/>=param;
                                            }
                                            else {
                                                throw new java.lang.RuntimeException();
                                            }
                                        </xsl:when>
                                        <xsl:when test="@maxExFacet or @minExFacet or @maxInFacet or @minInFacet">
                                            <xsl:if test="@maxExFacet">
                                                 if (org.apache.axis2.databinding.utils.ConverterUtil.compare(param, "<xsl:value-of select="$maxExFacet"/>") &lt; 0){
                                                        this.<xsl:value-of select="$varName"/>=param;
                                                    }
                                                    else {
                                                        throw new java.lang.RuntimeException();
                                                    }
                                            </xsl:if>
                                            <xsl:if test="@minExFacet">
                                                 if (org.apache.axis2.databinding.utils.ConverterUtil.compare(param, "<xsl:value-of select="$minExFacet"/>") &gt; 0){
                                                    this.<xsl:value-of select="$varName"/>=param;
                                                }
                                                else {
                                                    throw new java.lang.RuntimeException();
                                                }
                                            </xsl:if>
                                            <xsl:if test="@maxInFacet">
                                                if (org.apache.axis2.databinding.utils.ConverterUtil.compare(param, "<xsl:value-of select="$maxInFacet"/>") &lt;= 0){
                                                    this.<xsl:value-of select="$varName"/>=param;
                                                }
                                                else {
                                                    throw new java.lang.RuntimeException();
                                                }
                                            </xsl:if>
                                            <xsl:if test="@minInFacet">
                                                if (org.apache.axis2.databinding.utils.ConverterUtil.compare(param, "<xsl:value-of select="$minInFacet"/>") &gt;= 0){
                                                    this.<xsl:value-of select="$varName"/>=param;
                                                }
                                                else {
                                                    throw new java.lang.RuntimeException();
                                                }
                                            </xsl:if>
                                        </xsl:when>
                                        <xsl:otherwise>
                                             this.<xsl:value-of select="$varName"/>=param;
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    </xsl:when>

                                    <xsl:otherwise>
                                            this.<xsl:value-of select="$varName"/>=param;
                                    </xsl:otherwise>
                                </xsl:choose>

                               }
                            </xsl:otherwise>
                        </xsl:choose>
                        </xsl:if>

                         <!-- generate from string and to string methods for
                          simple types to be ued in attribute handling -->
                        <xsl:if test="$simple and not(@attribute) and not(enumFacet)">

                            public java.lang.String toString(){
                                <xsl:choose>
                                    <xsl:when test="(@primitive) or ($shortTypeName = 'DateTime') or ($shortTypeName = 'Date') or ($shortTypeName = 'Time')">
                                        return org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>);
                                    </xsl:when>
                                    <xsl:otherwise>
                                        return <xsl:value-of select="$varName"/>.toString();
                                    </xsl:otherwise>
                                </xsl:choose>
                            }
                        </xsl:if>

                      </xsl:otherwise>
                   </xsl:choose>

                </xsl:otherwise>
            </xsl:choose>

             <!-- end of xsl:if for not(@inherited) -->
            </xsl:if>

        </xsl:for-each>

        <!-- we don't need to generate the setObject method in parent classes -->
        <xsl:if test="$union and $simple and not($restriction) and not($extension)">
            <!-- generate class for a union type -->

              public void setObject(java.lang.Object object){
                  <xsl:for-each select="memberType">
                      <xsl:if test="position() > 1">} else </xsl:if>
                      if (object instanceof <xsl:value-of select="@type"/>){
                            this.localObject = object;
                  </xsl:for-each>
                      } else {
                          throw new java.lang.RuntimeException("Invalid object type");
                      }
              }

        </xsl:if>

        <xsl:if test="$list and not($restriction) and not($extension)" >
             <xsl:variable name="javaName"><xsl:value-of select="itemtype/@javaname"/></xsl:variable>
             <xsl:variable name="varName">local<xsl:value-of select="itemtype/@javaname"/></xsl:variable>
             <xsl:variable name="varType"><xsl:value-of select="itemtype/@type"/></xsl:variable>
             <xsl:variable name="primitive"><xsl:value-of select="itemtype/@primitive"/></xsl:variable>

             protected <xsl:value-of select="$varType"/>[]  <xsl:value-of select="$varName"/>;

             public <xsl:value-of select="$varType"/>[] get<xsl:value-of select="$javaName"/>(){
                return <xsl:value-of select="$varName"/>;
             }

             public void set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$varType"/>[] itemList){
                this.<xsl:value-of select="$varName"/> = itemList;
             }

            public java.lang.String toString() {
                java.lang.StringBuffer outString = new java.lang.StringBuffer();
                if (<xsl:value-of select="$varName"/> != null){
                    for(int i = 0; i &lt; <xsl:value-of select="$varName"/>.length;i++){
                        <xsl:choose>
                            <xsl:when test="$primitive">
                                outString.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i])).append(" ");
                            </xsl:when>
                            <xsl:otherwise>
                                outString.append(<xsl:value-of select="$varName"/>[i].toString()).append(" ");
                            </xsl:otherwise>
                        </xsl:choose>

                    }
                }
                return outString.toString().trim();
            }
        </xsl:if>

     <!-- ######################################################################################### -->
     <!-- get OMElement methods that allows direct writing -->
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        <xsl:choose>
            <xsl:when test="@type">
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,parentQName);
               return factory.createOMElement(dataSource,parentQName);
            </xsl:when>
            <xsl:otherwise>
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
               return factory.createOMElement(dataSource,MY_QNAME);
            </xsl:otherwise>
        </xsl:choose>
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            <xsl:choose>

            <xsl:when test="$simple and $union">
                writeStartElement(null, parentQName.getNamespaceURI(), parentQName.getLocalPart(), xmlWriter);

                <xsl:for-each select="memberType">
                      <xsl:if test="position() > 1">} else </xsl:if>
                      if (localObject instanceof <xsl:value-of select="@type"/>){
                           java.lang.String namespacePrefix = registerPrefix(xmlWriter,"<xsl:value-of select="@nsuri"/>");
                           if ((namespacePrefix != null) &amp;&amp; (namespacePrefix.trim().length() > 0)){
                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                                   namespacePrefix+":<xsl:value-of select="@originalName"/>",
                                   xmlWriter);
                           } else {
                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                                   "<xsl:value-of select="@originalName"/>",
                                   xmlWriter);
                           }

                       <xsl:choose>
                           <xsl:when test="@type='javax.xml.namespace.QName'">
                               writeQName((javax.xml.namespace.QName)localObject,xmlWriter);
                           </xsl:when>
                           <xsl:otherwise>
                               xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString((<xsl:value-of select="@type"/>)localObject));
                           </xsl:otherwise>
                       </xsl:choose>
                  </xsl:for-each>
                      } else {
                          throw new org.apache.axis2.databinding.ADBException("Invalid object type");
                      }
                xmlWriter.writeEndElement();
            </xsl:when>

            <xsl:when test="$simple and $list">

                 <xsl:variable name="javaName"><xsl:value-of select="itemtype/@javaname"/></xsl:variable>
                 <xsl:variable name="varType"><xsl:value-of select="itemtype/@type"/></xsl:variable>

                writeStartElement(null, parentQName.getNamespaceURI(), parentQName.getLocalPart(), xmlWriter);

                <xsl:choose>
                   <xsl:when test="$varType='javax.xml.namespace.QName'">
                        writeQNames(local<xsl:value-of select="$javaName"/>,xmlWriter);
                   </xsl:when>
                   <xsl:otherwise>
                       xmlWriter.writeCharacters(<xsl:value-of select="$name"/>.this.toString());
                   </xsl:otherwise>
                </xsl:choose>

                xmlWriter.writeEndElement();
            </xsl:when>

            <xsl:when test="@type or @anon">
                <!-- For a type write the passed in QName first
                 we create special particle classes for Sequence,Choice and all elements to
                 handle maxOccurs correctly. So these classes should not write parent Qname-->


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                <xsl:if test="not($particleClass)">

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                </xsl:if>


                <!-- write the type attribute if needed -->
               <xsl:if test="not($extension) or @anon">
                  if (serializeType){
               </xsl:if>

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"<xsl:value-of select="$nsuri"/>");
                   if ((namespacePrefix != null) &amp;&amp; (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":<xsl:value-of select="$originalName"/>",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "<xsl:value-of select="$originalName"/>",
                           xmlWriter);
                   }

               <xsl:if test="not($extension) or @anon">
                   }
               </xsl:if>
                <!--First serialize the attributes!-->
                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                     <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                     <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
                    <xsl:choose>
                        <!-- Note - It is assumed that any attributes are OMAttributes-->
                        <xsl:when test="@any and not(@array)">
                           writeAttribute(<xsl:value-of select="$varName"/>.getNamespace().getName(),
                                                     <xsl:value-of select="$varName"/>.getLocalName(),
                                                     <xsl:value-of select="$varName"/>.getAttributeValue(),
                                                     xmlWriter);
                        </xsl:when>
                        <xsl:when test="@any and @array">
                             if (<xsl:value-of select="$varName"/> != null) {
                                 for (int i=0;i &lt;<xsl:value-of select="$varName"/>.length;i++){
                                     writeAttribute(<xsl:value-of select="$varName"/>[i].getNamespace().getName(),
                                                    <xsl:value-of select="$varName"/>[i].getLocalName(),
                                                    <xsl:value-of select="$varName"/>[i].getAttributeValue(),xmlWriter);
                                     }
                             }
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="@ours">
                                    <!--  this can only be a simple type -->
                                    if (<xsl:value-of select="$varName"/> != null){
                                        writeAttribute("<xsl:value-of select="$namespace"/>",
                                           "<xsl:value-of select="$propertyName"/>",
                                           <xsl:value-of select="$varName"/>.toString(), xmlWriter);
                                    }
                                    <xsl:if test="not(@optional)">
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute <xsl:value-of select="$varName"/> is null");
                                      }
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                        <xsl:when test="@primitive">
                                            <xsl:choose>
                                               <xsl:when test="$usewrapperclasses">
                                                  if (true) {
                                               </xsl:when>
                                               <xsl:when test="$propertyType='int'">
                                                   if (<xsl:value-of select="$varName"/>!=java.lang.Integer.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:when test="$propertyType='long'">
                                                   if (<xsl:value-of select="$varName"/>!=java.lang.Long.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:when test="$propertyType='byte'">
                                                   if (<xsl:value-of select="$varName"/>!=java.lang.Byte.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:when test="$propertyType='double'">
                                                   if (!java.lang.Double.isNaN(<xsl:value-of select="$varName"/>)) {
                                               </xsl:when>
                                               <xsl:when test="$propertyType='float'">
                                                   if (!java.lang.Float.isNaN(<xsl:value-of select="$varName"/>)) {
                                               </xsl:when>
                                               <xsl:when test="$propertyType='short'">
                                                   if (<xsl:value-of select="$varName"/>!=java.lang.Short.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:otherwise>
                                                   if (true) {
                                               </xsl:otherwise>
                                           </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            if (<xsl:value-of select="$varName"/> != null){
                                        </xsl:otherwise>
                                    </xsl:choose>
                                        <xsl:choose>
                                            <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                                                writeQNameAttribute("<xsl:value-of select="$namespace"/>",
                                                         "<xsl:value-of select="$propertyName"/>",
                                                         <xsl:value-of select="$varName"/>, xmlWriter);

                                            </xsl:when>
                                            <xsl:otherwise>
                                                writeAttribute("<xsl:value-of select="$namespace"/>",
                                                         "<xsl:value-of select="$propertyName"/>",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>), xmlWriter);

                                            </xsl:otherwise>
                                        </xsl:choose>
                                      }
                                    <xsl:if test="not(@optional)">
                                      else {
                                          throw new org.apache.axis2.databinding.ADBException("required attribute <xsl:value-of select="$varName"/> is null");
                                      }
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>

                <!-- Now serialize the elements-->
                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                    <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
                    <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:variable name="settingTracker">local<xsl:value-of select="@javaname"/>Tracker</xsl:variable>

                    <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
                    <xsl:variable name="propertyBaseType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                    <xsl:variable name="particleClassType" select="@particleClassType"></xsl:variable>

                    <xsl:if test="$min=0 or $choice"> if (<xsl:value-of select="$settingTracker"/>){</xsl:if>
                    <xsl:choose>
                        <xsl:when test="@ours and not(@array) and not(@default)">
                            <xsl:choose>
                                <xsl:when test="@nillable">
                                    if (<xsl:value-of select="$varName"/>==null){

                                        writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                       // write the nil attribute
                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                      xmlWriter.writeEndElement();
                                    }else{
                                     <xsl:value-of select="$varName"/>.serialize(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                        xmlWriter);
                                    }
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:choose>
                                        <xsl:when test="$particleClassType">
                                            if (<xsl:value-of select="$varName"/>==null){
                                                 throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                            }
                                           <xsl:value-of select="$varName"/>.serialize(null,xmlWriter);
                                        </xsl:when>
                                        <xsl:otherwise>
                                            if (<xsl:value-of select="$varName"/>==null){
                                                 throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                            }
                                           <xsl:value-of select="$varName"/>.serialize(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                               xmlWriter);
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="@ours and @array and not(@default)">
                             <xsl:choose>
                                 <xsl:when test="$particleClassType">
                                     <!-- if it is a particle clase that can only be minOccurs zero or not -->
                                      if (<xsl:value-of select="$varName"/>!=null){
                                            for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                                                if (<xsl:value-of select="$varName"/>[i] != null){
                                                 <xsl:value-of select="$varName"/>[i].serialize(null,xmlWriter);
                                                } else {
                                                   <xsl:choose>
                                                    <xsl:when test="$min=0">
                                                        // we don't have to do any thing since minOccures is zero
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                           throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                }

                                            }
                                     } else {
                                        throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                     }
                                 </xsl:when>
                                 <xsl:otherwise>
                                       if (<xsl:value-of select="$varName"/>!=null){
                                            for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                                                if (<xsl:value-of select="$varName"/>[i] != null){
                                                 <xsl:value-of select="$varName"/>[i].serialize(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                                           xmlWriter);
                                                } else {
                                                   <xsl:choose>
                                                    <xsl:when test="@nillable">
                                                            writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                                           // write the nil attribute
                                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                           xmlWriter.writeEndElement();
                                                    </xsl:when>
                                                    <xsl:when test="$min=0">
                                                        // we don't have to do any thing since minOccures is zero
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                           throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                }

                                            }
                                     } else {
                                        <xsl:choose>
                                        <xsl:when test="@nillable">
                                                writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                               // write the nil attribute
                                               writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                               xmlWriter.writeEndElement();
                                        </xsl:when>
                                        <xsl:otherwise>
                                               throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    }
                                 </xsl:otherwise>
                             </xsl:choose>

                        </xsl:when>

                        <xsl:when test="@default and @array">
                            <!-- Note - Assumed to be OMElement-->
                            if (<xsl:value-of select="$varName"/>!=null){
                                 for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                                    if (<xsl:value-of select="$varName"/>[i] != null){

                                           if (<xsl:value-of select="$varName"/>[i] instanceof org.apache.axis2.databinding.ADBBean){
                                                ((org.apache.axis2.databinding.ADBBean)<xsl:value-of select="$varName"/>[i]).serialize(
                                                           new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                                           xmlWriter,true);
                                            } else {
                                                writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);
                                                org.apache.axis2.databinding.utils.ConverterUtil.serializeAnyType(<xsl:value-of select="$varName"/>[i], xmlWriter);
                                                xmlWriter.writeEndElement();
                                             }

                                    } else {
                                       <xsl:choose>
                                       <xsl:when test="@nillable">
                                            // write null attribute
                                            writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                        </xsl:when>
                                        <xsl:when test="$min=0">
                                              // we have to do nothing since minOccurs is zero
                                        </xsl:when>
                                        <xsl:otherwise>
                                             throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    }
                                 }
                            } else {
                                 <xsl:choose>
                                   <xsl:when test="@nillable">
                                        // write null attribute
                                        writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                       // write the nil attribute
                                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                       xmlWriter.writeEndElement();
                                    </xsl:when>
                                    <xsl:otherwise>
                                         throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    </xsl:otherwise>
                                </xsl:choose>
                            }

                        </xsl:when>

                        <xsl:when test="@default and not(@array)">
                            <!-- Note - Assumed to be OMElement-->
                            if (<xsl:value-of select="$varName"/>!=null){
                                if (<xsl:value-of select="$varName"/> instanceof org.apache.axis2.databinding.ADBBean){
                                    ((org.apache.axis2.databinding.ADBBean)<xsl:value-of select="$varName"/>).serialize(
                                               new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"),
                                               xmlWriter,true);
                                 } else {
                                    writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);
                                    org.apache.axis2.databinding.utils.ConverterUtil.serializeAnyType(<xsl:value-of select="$varName"/>, xmlWriter);
                                    xmlWriter.writeEndElement();
                                 }
                            } else {
                                <xsl:choose>
                                 <xsl:when test="@nillable">
                                        // write null attribute
                                           writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    </xsl:when>
                                    <xsl:otherwise>
                                         throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    </xsl:otherwise>
                                 </xsl:choose>
                            }


                        </xsl:when>
                        <!-- handle non ADB arrays - Not any however -->
                        <xsl:when test="@array and not(@any)">
                             if (<xsl:value-of select="$varName"/>!=null) {
                                   namespace = "<xsl:value-of select="$namespace"/>";
                                   for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                                        <xsl:if test="@primitive">
                                            <xsl:choose>
                                               <xsl:when test="$usewrapperclasses">
                                                  if (true) {
                                               </xsl:when>
                                               <xsl:when test="$propertyBaseType='int'">
                                                   if (<xsl:value-of select="$varName"/>[i]!=java.lang.Integer.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:when test="$propertyBaseType='long'">
                                                   if (<xsl:value-of select="$varName"/>[i]!=java.lang.Long.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:when test="$propertyBaseType='byte'">
                                                   if (<xsl:value-of select="$varName"/>[i]!=java.lang.Byte.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:when test="$propertyBaseType='double'">
                                                   if (!java.lang.Double.isNaN(<xsl:value-of select="$varName"/>[i])) {
                                               </xsl:when>
                                               <xsl:when test="$propertyBaseType='float'">
                                                   if (!java.lang.Float.isNaN(<xsl:value-of select="$varName"/>[i])) {
                                               </xsl:when>
                                               <xsl:when test="$propertyBaseType='short'">
                                                   if (<xsl:value-of select="$varName"/>[i]!=java.lang.Short.MIN_VALUE) {
                                               </xsl:when>
                                               <xsl:otherwise>
                                                   if (true) {
                                               </xsl:otherwise>
                                           </xsl:choose>
                                        </xsl:if>
                                        <xsl:if test="not(@primitive)">
                                            if (<xsl:value-of select="$varName"/>[i] != null){
                                        </xsl:if>
                                                writeStartElement(null, namespace, "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                            <xsl:if test="@primitive">
                                                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                                                xmlWriter.writeEndElement();
                                            </xsl:if>

                                            <xsl:if test="not(@primitive)">
                                                <xsl:choose>
                                                    <xsl:when test="@binary">
                                                        try {
                                                            org.apache.axiom.util.stax.XMLStreamWriterUtils.writeDataHandler(xmlWriter, <xsl:value-of select="$varName"/>[i], null, true);
                                                        } catch (java.io.IOException ex) {
                                                            throw new javax.xml.stream.XMLStreamException("Unable to read data handler for <xsl:value-of select="$propertyName"/>[" + i + "]", ex);
                                                        }
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                                xmlWriter.writeEndElement();
                                              </xsl:if>
                                                } else {
                                                   <xsl:choose>
                                                       <xsl:when test="@nillable">
                                                           // write null attribute
                                                            namespace = "<xsl:value-of select="$namespace"/>";
                                                            writeStartElement(null, namespace, "<xsl:value-of select="$propertyName"/>", xmlWriter);
                                                            writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                            xmlWriter.writeEndElement();
                                                       </xsl:when>
                                                       <xsl:when test="$min=0">
                                                           // we have to do nothing since minOccurs is zero
                                                       </xsl:when>
                                                       <xsl:otherwise>
                                                           throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                                       </xsl:otherwise>

                                                   </xsl:choose>
                                                }

                                   }
                             } else {
                                 <xsl:choose>
                                    <xsl:when test="@nillable">
                                         // write the null attribute
                                        // write null attribute
                                           writeStartElement(null, "<xsl:value-of select="$namespace"/>", "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                           // write the nil attribute
                                           writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                           xmlWriter.writeEndElement();
                                    </xsl:when>
                                    <xsl:otherwise>
                                         throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    </xsl:otherwise>
                                </xsl:choose>
                             }

                        </xsl:when>

                         <!-- handle non ADB arrays  - Any case  - any may not be
                         nillable -->
                        <xsl:when test="@array and @any">
                            <!-- Note - Assumed to be OMElement-->
                            if (<xsl:value-of select="$varName"/> != null){
                                for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                                    if (<xsl:value-of select="$varName"/>[i] != null){
                                        <xsl:value-of select="$varName"/>[i].serialize(xmlWriter);
                                    } else {
                                        <xsl:choose>
                                            <xsl:when test="$min=0">
                                                // we have to do nothing since minOccures zero
                                            </xsl:when>
                                            <xsl:otherwise>
                                               throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    }
                                }
                            } else {
                                throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                            }
                        </xsl:when>
                        <!-- handle any - non array case-->
                         <xsl:when test="@any">
                            <!-- Note - Assumed to be OMElement-->
                            if (<xsl:value-of select="$varName"/> != null) {
                                <xsl:value-of select="$varName"/>.serialize(xmlWriter);
                            } else {
                               throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                            }
                        </xsl:when>
                        <!-- handle all other cases -->
                         <xsl:otherwise>
                             <xsl:if test="not($simple)">
                                    namespace = "<xsl:value-of select="$namespace"/>";
                                    writeStartElement(null, namespace, "<xsl:value-of select="$propertyName"/>", xmlWriter);
                             </xsl:if>
                             <xsl:choose>
                                 <!-- handle the binary case -->
                                 <xsl:when test="@binary">
                                        <!-- Handling the null byte array -->
                                    if (<xsl:value-of select="$varName"/>!=null)  {
                                       try {
                                           org.apache.axiom.util.stax.XMLStreamWriterUtils.writeDataHandler(xmlWriter, <xsl:value-of select="$varName"/>, null, true);
                                       } catch (java.io.IOException ex) {
                                           throw new javax.xml.stream.XMLStreamException("Unable to read data handler for <xsl:value-of select="$propertyName"/>", ex);
                                       }
                                    } else {
                                         <xsl:if test="@nillable">
                                             writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                         </xsl:if>
                                    }
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:if test="not(@primitive)">

                                          if (<xsl:value-of select="$varName"/>==null){
                                              // write the nil attribute
                                              <xsl:choose>
                                                  <xsl:when test="@nillable">
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                  </xsl:when>
                                                  <xsl:otherwise>
                                                     throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                                  </xsl:otherwise>
                                              </xsl:choose>
                                          }else{

                                        <xsl:choose>
                                            <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                                                writeQName(<xsl:value-of select="$varName"/>,xmlWriter);
                                            </xsl:when>
                                            <xsl:when test="$propertyType='org.apache.axiom.om.OMElement'">
                                                <xsl:value-of select="$varName"/>.serialize(xmlWriter);
                                            </xsl:when>
                                            <xsl:when test="$propertyType='java.lang.String'">
                                                   xmlWriter.writeCharacters(<xsl:value-of select="$varName"/>);
                                            </xsl:when>
                                            <xsl:otherwise>
                                                   xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                            </xsl:otherwise>
                                        </xsl:choose>
                                          }
                                    </xsl:if>
                                    <xsl:if test="@primitive">
                                       <!-- we have to check for nillability with min value -->
                                       <xsl:choose>
                                           <xsl:when test="$usewrapperclasses">
                                                  if (false) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='int'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Integer.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='long'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Long.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='byte'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Byte.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='double'">
                                               if (java.lang.Double.isNaN(<xsl:value-of select="$varName"/>)) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='float'">
                                               if (java.lang.Float.isNaN(<xsl:value-of select="$varName"/>)) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='short'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Short.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:otherwise>
                                               if (false) {
                                           </xsl:otherwise>
                                       </xsl:choose>
                                                <xsl:choose>
                                                      <xsl:when test="@nillable">
                                                         writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                                      </xsl:otherwise>
                                                  </xsl:choose>
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                               }
                                    </xsl:if>
                                 </xsl:otherwise>
                             </xsl:choose>
                             <xsl:if test="not($simple)">
                                   xmlWriter.writeEndElement();
                             </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$min=0 or $choice">}</xsl:if>

                </xsl:for-each>
                   <!-- write the end element for the type-->
               <xsl:if test="not($particleClass)">
                    xmlWriter.writeEndElement();
               </xsl:if>
            <!-- end of when for type & anon -->
            </xsl:when>

            <!-- Not a type and not anon. So it better be only one inclusion-->
            <xsl:otherwise>
                <!-- if the element is associated with a type, then its gonna be only one -->
                //We can safely assume an element has only one type associated with it
                <xsl:variable name="varName">local<xsl:value-of select="property/@javaname"/></xsl:variable>
                <xsl:variable name="nillable" select="property/@nillable"></xsl:variable>
                <xsl:variable name="primitive" select="property/@primitive"></xsl:variable>
                <xsl:variable name="propertyType"><xsl:value-of select="property/@type"/></xsl:variable>
                <xsl:variable name="propertyName"><xsl:value-of select="property/@name"/></xsl:variable>

                <xsl:choose>
                    <!-- This better be only one!!-->
                    <xsl:when test="property/@ours">
                        <xsl:choose>
                            <xsl:when test="$nillable">
                                      if (<xsl:value-of select="$varName"/>==null){
                                        java.lang.String namespace = "<xsl:value-of select="property/@nsuri"/>";
                                        writeStartElement(null, namespace, "<xsl:value-of select="$propertyName"/>", xmlWriter);

                                        // write the nil attribute
                                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                        xmlWriter.writeEndElement();
                                       }else{
                                         <xsl:value-of select="$varName"/>.serialize(MY_QNAME,xmlWriter);
                                       }
                            </xsl:when>
                            <xsl:otherwise>
                                 if (<xsl:value-of select="$varName"/>==null){
                                   throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!");
                                 }
                                 <xsl:value-of select="$varName"/>.serialize(MY_QNAME,xmlWriter);
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <!-- end of ours block-->
                    <xsl:otherwise>
                        <xsl:if test="not(property/@simple)">
                            java.lang.String namespace = "<xsl:value-of select="property/@nsuri"/>";
                            java.lang.String _localName = "<xsl:value-of select="$propertyName"/>";
                        </xsl:if>
                        <xsl:if test="property/@simple">
                            java.lang.String namespace = parentQName.getNamespaceURI();
                            java.lang.String _localName = parentQName.getLocalPart();
                        </xsl:if>
                            writeStartElement(null, namespace, _localName, xmlWriter);

                            // add the type details if this is used in a simple type
                               if (serializeType){
                                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"<xsl:value-of select="$nsuri"/>");
                                   if ((namespacePrefix != null) &amp;&amp; (namespacePrefix.trim().length() > 0)){
                                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                                           namespacePrefix+":<xsl:value-of select="$originalName"/>",
                                           xmlWriter);
                                   } else {
                                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                                           "<xsl:value-of select="$originalName"/>",
                                           xmlWriter);
                                   }
                               }
                            <xsl:if test="not($primitive)">
                                          if (<xsl:value-of select="$varName"/>==null){
                                            <xsl:choose>
                                                <xsl:when test="$nillable">
                                                     // write the nil attribute
                                                     writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                </xsl:when>
                                                <xsl:otherwise>
                                                     throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null !!");
                                                </xsl:otherwise>
                                            </xsl:choose>
                                         }else{
                                        <xsl:choose>
                                            <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                                                writeQName(<xsl:value-of select="$varName"/>,xmlWriter);
                                            </xsl:when>
                                            <xsl:when test="$propertyType='org.apache.axiom.om.OMElement'">
                                                <xsl:value-of select="$varName"/>.serialize(xmlWriter);
                                            </xsl:when>
                                            <xsl:when test="$propertyType='java.lang.String'">
                                                       xmlWriter.writeCharacters(<xsl:value-of select="$varName"/>);
                                            </xsl:when>
                                            <xsl:when test="property/@default">
                                                 org.apache.axis2.databinding.utils.ConverterUtil.serializeAnyType(<xsl:value-of select="$varName"/>, xmlWriter);
                                            </xsl:when>
                                            <xsl:when test="property/@binary">
                                                    <!-- Handling the null byte array -->
                                                if (<xsl:value-of select="$varName"/>!=null)
                                                {
                                                    try {
                                                        org.apache.axiom.util.stax.XMLStreamWriterUtils.writeDataHandler(xmlWriter, <xsl:value-of select="$varName"/>, null, true);
                                                    } catch (java.io.IOException ex) {
                                                        throw new javax.xml.stream.XMLStreamException("Unable to read data handler for <xsl:value-of select="$propertyName"/>", ex);
                                                    }
                                                }
                                             </xsl:when>
                                            <xsl:otherwise>
                                                       xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                            </xsl:otherwise>
                                        </xsl:choose>
                                         }
                                    </xsl:if>
                            <xsl:if test="$primitive">

                               <!-- we have to check for nillability with min value -->
                                       <xsl:choose>
                                           <xsl:when test="$usewrapperclasses">
                                                  if (false) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='int'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Integer.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='long'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Long.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='byte'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Byte.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='double'">
                                               if (java.lang.Double.isNaN(<xsl:value-of select="$varName"/>)) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='float'">
                                               if (java.lang.Float.isNaN(<xsl:value-of select="$varName"/>)) {
                                           </xsl:when>
                                           <xsl:when test="$propertyType='short'">
                                               if (<xsl:value-of select="$varName"/>==java.lang.Short.MIN_VALUE) {
                                           </xsl:when>
                                           <xsl:otherwise>
                                               if (false) {
                                           </xsl:otherwise>
                                       </xsl:choose>
                                                <xsl:choose>
                                                      <xsl:when test="@nillable">
                                                         writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                      </xsl:when>
                                                      <xsl:otherwise>
                                                         throw new org.apache.axis2.databinding.ADBException("property value cannot be null!!");
                                                      </xsl:otherwise>
                                                  </xsl:choose>
                                               } else {
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                               }
                            </xsl:if>
                            xmlWriter.writeEndElement();
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("<xsl:value-of select="$nsuri"/>")){
                return "<xsl:value-of select="$nsprefix"/>";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i &lt; qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  <!-- ######################################################################################### -->
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        <xsl:choose>
            <xsl:when test="@type or @anon">
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                <xsl:if test="$extension">
                    attribList.add(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema-instance","type"));
                    attribList.add(new javax.xml.namespace.QName("<xsl:value-of select="$nsuri"/>","<xsl:value-of select="$originalName"/>"));
                </xsl:if>

                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                    <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
                    <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:variable name="settingTracker">local<xsl:value-of select="@javaname"/>Tracker</xsl:variable>


                    <xsl:if test="$min=0 or $choice"> if (<xsl:value-of select="$settingTracker"/>){</xsl:if>
                    <xsl:choose>
                        <xsl:when test="(@ours or @default) and not(@array)">
                            elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                            <!-- Arraylist can handle null's -->
                            <xsl:choose>
                                <xsl:when test="@nillable">
                                    elementList.add(<xsl:value-of select="$varName"/>==null?null:
                                    <xsl:value-of select="$varName"/>);
                                </xsl:when>
                                <xsl:otherwise>
                                    if (<xsl:value-of select="$varName"/>==null){
                                         throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    }
                                    elementList.add(<xsl:value-of select="$varName"/>);
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="(@ours or @default) and @array">
                             if (<xsl:value-of select="$varName"/>!=null) {
                                 for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){

                                    if (<xsl:value-of select="$varName"/>[i] != null){
                                         elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                          "<xsl:value-of select="$propertyName"/>"));
                                         elementList.add(<xsl:value-of select="$varName"/>[i]);
                                    } else {
                                        <xsl:choose>
                                            <xsl:when test="@nillable">
                                                elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                          "<xsl:value-of select="$propertyName"/>"));
                                                elementList.add(null);
                                            </xsl:when>
                                            <xsl:when test="$min=0">
                                                // nothing to do
                                            </xsl:when>
                                            <xsl:otherwise>
                                               throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null !!");
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    }

                                 }
                             } else {
                                 <xsl:choose>
                                    <xsl:when test="@nillable">
                                        elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                          "<xsl:value-of select="$propertyName"/>"));
                                        elementList.add(<xsl:value-of select="$varName"/>);
                                    </xsl:when>
                                    <xsl:otherwise>
                                        throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                    </xsl:otherwise>
                                </xsl:choose>
                             }

                        </xsl:when>
                        <!-- handle non ADB arrays - Not any however -->
                        <xsl:when test="@array and not(@any)">
                            if (<xsl:value-of select="$varName"/>!=null){
                                  for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                                      <xsl:if test="not(@primitive)">
                                         if (<xsl:value-of select="$varName"/>[i] != null){
                                          elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                              "<xsl:value-of select="$propertyName"/>"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                                          } else {
                                             <xsl:choose>
                                                <xsl:when test="@nillable">
                                                    elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                              "<xsl:value-of select="$propertyName"/>"));
                                                    elementList.add(null);
                                                </xsl:when>
                                                <xsl:when test="$min=0">
                                                    // have to do nothing
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                                </xsl:otherwise>
                                            </xsl:choose>
                                          }
                                      </xsl:if>
                                      <xsl:if test="@primitive">
                                          elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                                                                       "<xsl:value-of select="$propertyName"/>"));
                                          elementList.add(
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));

                                      </xsl:if>

                                  }
                            } else {
                              <xsl:choose>
                                <xsl:when test="@nillable">
                                    elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                              "<xsl:value-of select="$propertyName"/>"));
                                    elementList.add(null);
                                </xsl:when>
                                <xsl:otherwise>
                                    throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                </xsl:otherwise>
                               </xsl:choose>
                            }

                        </xsl:when>

                         <!-- handle non ADB arrays  - Any case  - any may not be
                         nillable -->
                        <xsl:when test="@array and @any">
                            if (<xsl:value-of select="$varName"/> != null) {
                                for (int i = 0;i &lt; <xsl:value-of select="$varName"/>.length;i++){
                                    if (<xsl:value-of select="$varName"/>[i] != null){
                                       elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                          "<xsl:value-of select="$propertyName"/>"));
                                      elementList.add(
                                      org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>[i]));
                                    } else {
                                        <xsl:choose>
                                            <xsl:when test="$min=0">
                                                // have to do nothing
                                            </xsl:when>
                                            <xsl:otherwise>
                                                throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    }

                                }
                            } else {
                               throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                            }
                        </xsl:when>
                        <!-- handle any - non array case-->
                         <xsl:when test="@any">
                            if (<xsl:value-of select="$varName"/> != null){
                                elementList.add(org.apache.axis2.databinding.utils.Constants.OM_ELEMENT_KEY);
                                elementList.add(<xsl:value-of select="$varName"/>);
                            } else {
                               throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                            }
                        </xsl:when>
                        <!-- handle binary - Since it is a Datahandler, we can just add it to the list
                          and the ADB pullparser would handle it right-->
                         <xsl:when test="@binary">
                            <xsl:choose>
                                <xsl:when test="$simple">
                                      elementList.add(org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT);
                                </xsl:when>
                                <xsl:otherwise>
                                      elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                        "<xsl:value-of select="$propertyName"/>"));
                                </xsl:otherwise>
                            </xsl:choose>
                            elementList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                        <!-- the usual case!!!!-->
                        <xsl:otherwise>
                             <xsl:choose>
                                 <xsl:when test="$simple">
                                     <!-- if the type is simple then  this must be only the element text -->
                                     elementList.add(org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT);
                                 </xsl:when>
                                 <xsl:otherwise>
                                      elementList.add(new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>",
                                                                      "<xsl:value-of select="$propertyName"/>"));
                                 </xsl:otherwise>
                             </xsl:choose>

                            <xsl:if test="@primitive">
                                elementList.add(
                                   org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                            </xsl:if>
                            <xsl:if test="not(@primitive)">
                                 <xsl:choose>
                                    <xsl:when test="@nillable">
                                         elementList.add(<xsl:value-of select="$varName"/>==null?null:
                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                    </xsl:when>
                                    <xsl:otherwise>
                                        if (<xsl:value-of select="$varName"/> != null){
                                            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                        } else {
                                           throw new org.apache.axis2.databinding.ADBException("<xsl:value-of select="$propertyName"/> cannot be null!!");
                                        }
                                    </xsl:otherwise>
                                </xsl:choose>
                           </xsl:if>

                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="$min=0 or $choice">}</xsl:if>
                </xsl:for-each>

                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                    <xsl:variable name="varName">local<xsl:value-of select="@javaname"/></xsl:variable>
                     <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                    <xsl:choose>
                        <xsl:when test="@any and not(@array)">
                            attribList.add(org.apache.axis2.databinding.utils.Constants.OM_ATTRIBUTE_KEY);
                            attribList.add(<xsl:value-of select="$varName"/>);
                        </xsl:when>
                         <xsl:when test="@any and @array">
                             for (int i=0;i &lt;<xsl:value-of select="$varName"/>.length;i++){
                               attribList.add(org.apache.axis2.databinding.utils.Constants.OM_ATTRIBUTE_KEY);
                               attribList.add(<xsl:value-of select="$varName"/>[i]);
                             }
                         </xsl:when>
                        <xsl:otherwise>
                            attribList.add(
                            new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>"));
                            <xsl:choose>
                                <xsl:when test="@ours">
                                      attribList.add(<xsl:value-of select="$varName"/>.toString());
                                </xsl:when>
                                <xsl:otherwise>
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>));
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            <!-- end of when for type & anon -->
            </xsl:when>
            <xsl:when test="$union and $simple">
                  return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME,
                    new java.lang.Object[]{
                    org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                    localObject.toString()
                    },
                    null);
            </xsl:when>

            <xsl:when test="$list and $simple">
                  return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME,
                    new java.lang.Object[]{
                    org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                    toString()
                    },
                    null);
            </xsl:when>

            <!-- Not a type and not anon. So it better be only one inclusion-->
            <xsl:otherwise>
                <!-- if the element is associated with a type, then its gonna be only one -->
                //We can safely assume an element has only one type associated with it
                <xsl:variable name="varName">local<xsl:value-of select="property/@javaname"/></xsl:variable>
                <xsl:variable name="nillable" select="property/@nillable"></xsl:variable>
                <xsl:variable name="primitive" select="property/@primitive"></xsl:variable>

                <xsl:choose>
                    <!-- This better be only one!!-->
                    <xsl:when test="property/@ours">

                        <xsl:choose>
                            <xsl:when test="$nillable">
                                if (<xsl:value-of select="$varName"/>==null){
                                   return new org.apache.axis2.databinding.utils.reader.NullXMLStreamReader(MY_QNAME);
                                }else{
                                   return <xsl:value-of select="$varName"/>.getPullParser(MY_QNAME);
                                }
                            </xsl:when>
                            <xsl:otherwise>return <xsl:value-of select="$varName"/>.getPullParser(MY_QNAME);</xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$nillable and not($primitive)">
                                if (<xsl:value-of select="$varName"/>==null){
                                      return new org.apache.axis2.databinding.utils.reader.NullXMLStreamReader(MY_QNAME);
                                }else{
                                   return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME,
                                       new java.lang.Object[]{
                                      org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                                       org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                                       },
                                       null);
                                }
                            </xsl:when>
                            <xsl:otherwise> return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(MY_QNAME,
                            new java.lang.Object[]{
                            org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader.ELEMENT_TEXT,
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(<xsl:value-of select="$varName"/>)
                            },
                            null);</xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        }

  <!-- ######################################################################################### -->

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        <!-- generate some utility factory methods here we must add these methods to a factory class
         since otherwise it gives a compilation exception in jdk 1.4 -->
        <xsl:if test="$union and $simple">
            <!-- generate methods for a union type -->
              public static <xsl:value-of select="$name"/> fromString(javax.xml.stream.XMLStreamReader xmlStreamReader,
                                                     java.lang.String namespaceURI,
                                                     java.lang.String type) throws org.apache.axis2.databinding.ADBException {

                    <xsl:value-of select="$name"/> object = null;
                    try {
                        if ("http://www.w3.org/2001/XMLSchema".equals(namespaceURI)) {
                            object = new <xsl:value-of select="$name"/>();
                            object.setObject(xmlStreamReader, namespaceURI, type);
                        } else {
                            object = new <xsl:value-of select="$name"/>();
                            object.setObject(<xsl:value-of select="$mapperClass"/>.getTypeObject(namespaceURI, type, xmlStreamReader));
                        }
                        return object;
                    } catch (java.lang.Exception e) {
                        throw new org.apache.axis2.databinding.ADBException("Error in parsing value");
                    }
               }

               public static <xsl:value-of select="$name"/> fromString(java.lang.String value,
                                                        java.lang.String namespaceURI){
                    <xsl:value-of select="$name"/> object = new <xsl:value-of select="$name"/>();
                    boolean isValueSet = false;
                    <xsl:for-each select="memberType">
                      // we have to set the object with the first matching type.
                      if (!isValueSet) {
                        <xsl:choose>
                            <xsl:when test="@nsuri='http://www.w3.org/2001/XMLSchema'">
                                try {
                                    java.lang.reflect.Method converterMethod =
                                            org.apache.axis2.databinding.utils.ConverterUtil.class.getMethod(
                                                    "convertTo<xsl:value-of select="@shorttypename"/>",
                                                    new java.lang.Class[]{java.lang.String.class});
                                    object.setObject(converterMethod.invoke(null, new java.lang.Object[]{value}));
                                    isValueSet = true;
                                } catch (java.lang.Exception e) {
                                }
                            </xsl:when>
                            <xsl:otherwise>
                                try {
                                   object.setObject(<xsl:value-of select="@type"/>.Factory.fromString(value, namespaceURI));
                                   isValueSet = true;
                                } catch (java.lang.Exception e) {
                                }
                            </xsl:otherwise>
                        </xsl:choose>
                      }
                    </xsl:for-each>
                    return object;
                }

                public static <xsl:value-of select="$name"/> fromString(javax.xml.stream.XMLStreamReader xmlStreamReader,
                                                                    java.lang.String content) {
                    if (content.indexOf(":") > -1){
                        java.lang.String prefix = content.substring(0,content.indexOf(":"));
                        java.lang.String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
                        return <xsl:value-of select="$name"/>.Factory.fromString(content,namespaceUri);
                    } else {
                       return <xsl:value-of select="$name"/>.Factory.fromString(content,"");
                    }
                }

        </xsl:if>

        <xsl:if test="$list and $simple">

             <xsl:variable name="javaName"><xsl:value-of select="itemtype/@javaname"/></xsl:variable>
             <xsl:variable name="varType"><xsl:value-of select="itemtype/@type"/></xsl:variable>
             <xsl:variable name="ours"><xsl:value-of select="itemtype/@ours"/></xsl:variable>
            <xsl:variable name="shortTypeNameUncapped"  select="itemtype/@shorttypename"/>
            <xsl:variable name="shortTypeName"
               select="concat(translate( substring($shortTypeNameUncapped, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($shortTypeNameUncapped, 2, string-length($shortTypeNameUncapped)))" />


            public static <xsl:value-of select="$name"/> fromString(javax.xml.stream.XMLStreamReader xmlStreamReader,
                                                                    java.lang.String content)
                                                                    throws org.apache.axis2.databinding.ADBException {

                <xsl:value-of select="$name"/> object = new <xsl:value-of select="$name"/>();
                java.lang.String[] values = content.split(" +");
                <xsl:value-of select="$varType"/>[] objectValues = new <xsl:value-of select="$varType"/>[values.length];

                <xsl:if test="$varType='javax.xml.namespace.QName'">
                    java.lang.String prefix = null;
                    java.lang.String namespace = null;
                </xsl:if>

               <xsl:if test="string-length(normalize-space($ours)) > 0">
                    java.lang.String valueContent = null;
                    java.lang.String prefix = null;
                    java.lang.String namespace = null;
                </xsl:if>


                try {
                    for (int i = 0; i &lt; values.length; i++) {
                      <xsl:choose>
                          <xsl:when test="$varType='javax.xml.namespace.QName'">
                              if (values[i].indexOf(":") > 0){
                                 prefix = values[i].substring(0,values[i].indexOf(":"));
                              } else {
                                 prefix = "";
                              }
                              namespace = xmlStreamReader.getNamespaceURI(prefix);
                              objectValues[i] = org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(values[i],namespace);
                          </xsl:when>
                          <xsl:when test="string-length(normalize-space($ours)) > 0">
                           valueContent = values[i];
                           if (valueContent.indexOf(":") > 0){
                               prefix = valueContent.substring(0,valueContent.indexOf(":"));
                           } else {
                               prefix = "";
                           }
                           namespace = xmlStreamReader.getNamespaceURI(prefix);
                           objectValues[i] = <xsl:value-of select="$varType"/>.Factory.fromString(valueContent,namespace);
                          </xsl:when>
                          <xsl:otherwise>
                           objectValues[i] =
                              org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(values[i]);
                          </xsl:otherwise>
                      </xsl:choose>

                    }
                    object.set<xsl:value-of select="$javaName"/>(objectValues);
                    return object;
                } catch (java.lang.Exception e) {
                    throw new org.apache.axis2.databinding.ADBException();
                }

            }
        </xsl:if>

        <xsl:for-each select="property">
            <xsl:variable name="propertyType"><xsl:value-of select="@type"></xsl:value-of></xsl:variable>
            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
            <xsl:variable name="shortTypeNameUncapped"  select="@shorttypename"/>
            <xsl:variable name="shortTypeName"
               select="concat(translate( substring($shortTypeNameUncapped, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($shortTypeNameUncapped, 2, string-length($shortTypeNameUncapped)))" />

            <xsl:if test="$simple and not(@attribute) and not(enumFacet)">
                public static <xsl:value-of select="$name"/> fromString(java.lang.String value,
                                                    java.lang.String namespaceURI){
                    <xsl:value-of select="$name"/> returnValue = new  <xsl:value-of select="$name"/>();
                    <xsl:choose>
                        <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                            returnValue.set<xsl:value-of select="$javaName"/>(
                                org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(value,namespaceURI));
                        </xsl:when>
                        <xsl:otherwise>
                            returnValue.set<xsl:value-of select="$javaName"/>(
                                org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(value));
                        </xsl:otherwise>
                    </xsl:choose>

                    return returnValue;
                }

                public static <xsl:value-of select="$name"/> fromString(javax.xml.stream.XMLStreamReader xmlStreamReader,
                                                                    java.lang.String content) {
                    if (content.indexOf(":") > -1){
                        java.lang.String prefix = content.substring(0,content.indexOf(":"));
                        java.lang.String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
                        return <xsl:value-of select="$name"/>.Factory.fromString(content,namespaceUri);
                    } else {
                       return <xsl:value-of select="$name"/>.Factory.fromString(content,"");
                    }
                }

            </xsl:if>
            <xsl:if test="enumFacet">
                public static <xsl:value-of select="$name"/> fromValue(<xsl:value-of select="$propertyType"/> value)
                      throws java.lang.IllegalArgumentException {
                    <xsl:value-of select="$name"/> enumeration = (<xsl:value-of select="$name"/>)
                       <xsl:choose>
                           <xsl:when test="@primitive">
                             _table_.get(value + "");
                           </xsl:when>
                           <xsl:otherwise>
                               _table_.get(value);
                           </xsl:otherwise>
                       </xsl:choose>

                    if ((enumeration == null) &amp;&amp; !((value == null) || (value.equals("")))) {
                        throw new java.lang.IllegalArgumentException();
                    }
                    return enumeration;
                }
                public static <xsl:value-of select="$name"/> fromString(java.lang.String value,java.lang.String namespaceURI)
                      throws java.lang.IllegalArgumentException {
                    try {
                       <xsl:choose>
                           <xsl:when test="@primitive">
                             return fromValue(org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(value));
                           </xsl:when>
                           <xsl:otherwise>
                               <xsl:choose>
                                   <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                                       return fromValue(org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(value,namespaceURI));
                                   </xsl:when>
                                   <xsl:otherwise>
                                       return fromValue(org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(value));
                                   </xsl:otherwise>
                               </xsl:choose>
                           </xsl:otherwise>
                       </xsl:choose>

                    } catch (java.lang.Exception e) {
                        throw new java.lang.IllegalArgumentException();
                    }
                }

                public static <xsl:value-of select="$name"/> fromString(javax.xml.stream.XMLStreamReader xmlStreamReader,
                                                                    java.lang.String content) {
                    if (content.indexOf(":") > -1){
                        java.lang.String prefix = content.substring(0,content.indexOf(":"));
                        java.lang.String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
                        return <xsl:value-of select="$name"/>.Factory.fromString(content,namespaceUri);
                    } else {
                       return <xsl:value-of select="$name"/>.Factory.fromString(content,"");
                    }
                }
            </xsl:if>

        </xsl:for-each>

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static <xsl:value-of select="$name"/> parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            <xsl:variable name="isEnumFacet" select="property/enumFacet"/>
            <xsl:if test="not($isEnumFacet)"><xsl:value-of select="$name"/> object =
                <xsl:choose><xsl:when test="@isAbstract">null;</xsl:when><xsl:otherwise>new <xsl:value-of select="$name"/>();</xsl:otherwise></xsl:choose>
            </xsl:if>
            <xsl:if test="$isEnumFacet">
                <xsl:value-of select="$name"/> object = null;
                // initialize a hash map to keep values
                java.util.Map attributeMap = new java.util.HashMap();
                java.util.List extraAttributeList = new java.util.ArrayList&lt;org.apache.axiom.om.OMAttribute>();
            </xsl:if>

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                <!-- Advance to our start element, or if we are a complex type, to our first property start element or the outer end element if no properties -->
                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                    reader.next();

                <xsl:if test="@nillable">
                   nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                   if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                         // Skip the element and report the null value.  It cannot have subelements.
                         while (!reader.isEndElement())
                             reader.next();
                         <xsl:choose>
                             <xsl:when test="@type or @anon">
                                 return null;
                             </xsl:when>
                             <xsl:otherwise>
                                 <!-- this class can have only one property -->
                                 <xsl:variable name="propertyType"><xsl:value-of select="property/@type"/></xsl:variable>
                                 <xsl:variable name="javaName"><xsl:value-of select="property/@javaname"/></xsl:variable>
                                  <xsl:choose>
                                       <xsl:when test="$propertyType='int'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Integer.MIN_VALUE);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='long'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Long.MIN_VALUE);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='byte'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Byte.MIN_VALUE);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='double'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Double.NaN);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='float'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Float.NaN);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='short'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Short.MIN_VALUE);
                                       </xsl:when>
                                   </xsl:choose>
                                 return object;
                             </xsl:otherwise>
                         </xsl:choose>

                   }
                </xsl:if>
                <!-- partical classes can not have any types -->
                <xsl:if test="(($isType or $anon or $union) and not($particleClass))">
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    <xsl:choose>
                        <xsl:when test="$union">
                            java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                            object = <xsl:value-of select="$name"/>.Factory.fromString(reader,nsUri,type);
                        </xsl:when>
                        <xsl:otherwise>
                            if (!"<xsl:value-of select="$originalName"/>".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (<xsl:value-of select="$name"/>)<xsl:value-of select="$mapperClass"/>.getTypeObject(
                                     nsUri,type,reader);
                              }
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="@isAbstract">
                        throw new org.apache.axis2.databinding.ADBException("The an abstract class can not be instantiated !!!");
                    </xsl:if>

                  }
                <xsl:if test="$union">
                  } else {
                    // i.e this is an union type with out specific xsi:type
                    java.lang.String content = reader.getElementText();
                    if (content.indexOf(":") > -1){
                        // i.e. this could be a qname
                        prefix = content.substring(0,content.indexOf(":"));
                        namespaceuri = reader.getNamespaceContext().getNamespaceURI(prefix);
                        object = <xsl:value-of select="$name"/>.Factory.fromString(content,namespaceuri);
                    } else {
                        object = <xsl:value-of select="$name"/>.Factory.fromString(content,"");
                    }
                </xsl:if>

                }

                </xsl:if>

                <xsl:if test="$list">
                    java.lang.String listContent = reader.getElementText();
                    object = <xsl:value-of select="$name"/>.Factory.fromString(reader,listContent);
                </xsl:if>

                <!-- populate attributes here!!!. The attributes are part of an element, not part of a type -->
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                <!-- if this is an enumeration then we have to read attributes after-->

                <xsl:for-each select="property[@attribute]">
                    <xsl:variable name="propertyName" select="@name"/>
                    <xsl:variable name="propertyType" select="@type"/>
                    <xsl:variable name="shortTypeNameUncapped"  select="@shorttypename"/>
                    <xsl:variable name="shortTypeName"
                    select="concat(translate(substring($shortTypeNameUncapped, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($shortTypeNameUncapped, 2, string-length($shortTypeNameUncapped)))" />
                    <xsl:variable name="javaName" select="@javaname"/>
                    <xsl:variable name="namespace" select="@nsuri"/>
                    <xsl:variable name="attribName">tempAttrib<xsl:value-of select="$javaName"/></xsl:variable>

                    <xsl:if test="$propertyName != 'extraAttributes'">
                    // handle attribute "<xsl:value-of select="$propertyName"/>"
                    java.lang.String <xsl:value-of select="$attribName"/> =
                        <xsl:choose>
                            <xsl:when test="string-length(normalize-space($namespace)) > 0">
                                reader.getAttributeValue("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>");
                            </xsl:when>
                            <xsl:otherwise>
                                reader.getAttributeValue(null,"<xsl:value-of select="$propertyName"/>");
                            </xsl:otherwise>
                        </xsl:choose>
                   if (<xsl:value-of select="$attribName"/>!=null){
                         java.lang.String content = <xsl:value-of select="$attribName"/>;
                        <xsl:choose>
                            <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                                int index = <xsl:value-of select="$attribName"/>.indexOf(":");
                                if(index > -1){
                                     prefix = <xsl:value-of select="$attribName"/>.substring(0,index);
                                } else {
                                    // i.e this is in default namesace
                                    prefix = "";
                                }
                                namespaceuri = reader.getNamespaceURI(prefix);
                                 <xsl:choose>
                                     <xsl:when test="$isEnumFacet">
                                        attributeMap.put("<xsl:value-of select="$javaName"/>",
                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(<xsl:value-of select="$attribName"/>,namespaceuri));
                                     </xsl:when>
                                     <xsl:otherwise>
                                         object.set<xsl:value-of select="$javaName"/>(
                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(<xsl:value-of select="$attribName"/>,namespaceuri));
                                     </xsl:otherwise>
                                 </xsl:choose>

                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="$isEnumFacet">
                                        attributeMap.put("<xsl:value-of select="$javaName"/>",
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(<xsl:value-of select="$attribName"/>));
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:choose>
                                            <xsl:when test="@ours">
                                                  object.set<xsl:value-of select="$javaName"/>(
                                                        <xsl:value-of select="@type"/>.Factory.fromString(reader,<xsl:value-of select="$attribName"/>));
                                            </xsl:when>
                                            <xsl:otherwise>
                                                 object.set<xsl:value-of select="$javaName"/>(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(<xsl:value-of select="$attribName"/>));
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:otherwise>
                                </xsl:choose>
                             </xsl:otherwise>
                        </xsl:choose>
                    } else {
                       <xsl:choose>
                           <xsl:when test="@optional">
                               <xsl:if test="@primitive">
                                  <xsl:choose>
                                       <xsl:when test="$propertyType='int'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Integer.MIN_VALUE);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='long'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Long.MIN_VALUE);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='byte'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Byte.MIN_VALUE);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='double'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Double.NaN);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='float'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Float.NaN);
                                       </xsl:when>
                                       <xsl:when test="$propertyType='short'">
                                           object.set<xsl:value-of select="$javaName"/>(java.lang.Short.MIN_VALUE);
                                       </xsl:when>
                                   </xsl:choose>
                               </xsl:if>
                           </xsl:when>
                           <xsl:otherwise>
                               throw new org.apache.axis2.databinding.ADBException("Required attribute <xsl:value-of select="$propertyName"/> is missing");
                           </xsl:otherwise>
                       </xsl:choose>
                    }
                    handledAttributes.add("<xsl:value-of select="$propertyName"/>");
                    </xsl:if>

                    <!-- Handle anyAttributes here -->
                    <xsl:if test="$propertyName = 'extraAttributes'">
                        // now run through all any or extra attributes
                        // which were not reflected until now
                        for (int i=0; i &lt; reader.getAttributeCount(); i++) {
                            if (!handledAttributes.contains(reader.getAttributeLocalName(i))) {
                                // this is an anyAttribute and we create
                                // an OMAttribute for this
                                org.apache.axiom.om.OMFactory factory = org.apache.axiom.om.OMAbstractFactory.getOMFactory();
                                org.apache.axiom.om.OMAttribute attr =
                                    factory.createOMAttribute(
                                            reader.getAttributeLocalName(i),
                                            factory.createOMNamespace(
                                                reader.getAttributeNamespace(i), reader.getAttributePrefix(i)),
                                            reader.getAttributeValue(i));

                                // and add it to the extra attributes
                                <xsl:choose>
                                    <xsl:when test="property/enumFacet">
                                         extraAttributeList.add(attr);
                                    </xsl:when>
                                    <xsl:otherwise>
                                         object.addExtraAttributes(attr);
                                    </xsl:otherwise>
                                </xsl:choose>

                            }
                        }
                    </xsl:if>

                </xsl:for-each>

                <xsl:if test="($isType or $anon) and not($simple) and not($particleClass)">
                    <!-- Skip the outer start element in order to process the subelements. -->
                    reader.next();
                </xsl:if>
                      <!-- If we are not a type and not an element with anonymous type, then we are an element with one property for our named type. -->
                      <!-- Our single named-type property applies to our entire outer element, so don't skip it. -->
                <!-- First loop creates arrayLists for handling arrays -->
                <xsl:for-each select="property[not(@attribute)]">
                    <xsl:if test="@array">
                        java.util.ArrayList list<xsl:value-of select="position()"/> = new java.util.ArrayList();
                    </xsl:if>
                </xsl:for-each>

                <xsl:if test="property[not(@attribute)]">
                <xsl:if test="$unordered and not($particleClass)">   <!-- Properties can be in any order -->
                while(!reader.isEndElement()) {
                    if (reader.isStartElement() <xsl:if test="$simple"> || reader.hasText()</xsl:if>){
                </xsl:if>
                </xsl:if>

                        <!-- Now reloop and populate the code -->
                        <xsl:for-each select="property[not(@attribute)]">
                            <xsl:variable name="propertyName"><xsl:value-of select="@name"/></xsl:variable>
                            <xsl:variable name="propertyType"><xsl:value-of select="@type"/></xsl:variable>
                            <xsl:variable name="shortTypeNameUncapped"  select="@shorttypename"/>
                            <xsl:variable name="shortTypeName"
                                select="concat(translate(substring($shortTypeNameUncapped, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($shortTypeNameUncapped, 2, string-length($shortTypeNameUncapped)))" />
                            <xsl:variable name="javaName"><xsl:value-of select="@javaname"></xsl:value-of></xsl:variable>
                            <xsl:variable name="listName">list<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="loopBoolName">loopDone<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="startQname">startQname<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="stateMachineName">stateMachine<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="builderName">builder<xsl:value-of select="position()"/></xsl:variable>
                            <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                            <xsl:variable name="namespace"><xsl:value-of select="@nsuri"/></xsl:variable>
                            <xsl:variable name="min"><xsl:value-of select="@minOccurs"/></xsl:variable>
                            <xsl:variable name="particleClassType" select="@particleClassType"></xsl:variable>

                            <xsl:variable name="propQName">new javax.xml.namespace.QName("<xsl:value-of select="$namespace"/>","<xsl:value-of select="$propertyName"/>")</xsl:variable>

                           <xsl:choose>
                                <xsl:when test="$unordered and not($choice and $hasParticleType)">  <!-- One property per iteration if unordered -->
                                    <xsl:if test="position()>1">
                                        else
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <!-- If sequence, advance to start of next property or to end of outer element -->
                                    while (!reader.isStartElement() &amp;&amp; !reader.isEndElement()) reader.next();
                                </xsl:otherwise>
                            </xsl:choose>

                            <xsl:if test="not(enumFacet)">
                            <xsl:choose>
                                <xsl:when test="$shortTypeName='OMElement'">
                                   if (reader.isStartElement()){
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:if test="$particleClassType and ($choice or ($min=0))">
                                        <!-- since we can not validate the parser before going to next class
                                         we have to sollow an excpetions : todo find a better solsution-->
                                         try{
                                    </xsl:if>
                                    if (reader.isStartElement() <xsl:if test="$simple"> || reader.hasText()</xsl:if> <xsl:if test="not($simple) and not($particleClassType)">&amp;&amp; <xsl:value-of select="$propQName"/>.equals(reader.getName())</xsl:if>){
                                </xsl:otherwise>
                            </xsl:choose>

                            </xsl:if>
                            <xsl:choose>
                                <xsl:when test="@array">
                                    <!-- We must be a named type or element with anonymous type. -->
                                    <!-- Elements with a named type have a single simple (non-array) property for their type -->
                                    // Process the array and step past its final element's end.
                                    <xsl:variable name="basePropertyType"><xsl:value-of select="@arrayBaseType"/></xsl:variable>
                                    <xsl:choose>
                                        <xsl:when test="@ours">
                                             <xsl:choose>
                                                 <xsl:when test="$particleClassType">
                                                        <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                                                        //loop until we find a start element that is not part of this array
                                                        boolean <xsl:value-of select="$loopBoolName"/> = false;
                                                        while(!<xsl:value-of select="$loopBoolName"/>){

                                                            // Step to next element event.
                                                            while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                <xsl:value-of select="$loopBoolName"/> = true;
                                                            } else {
                                                                <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                <xsl:value-of select="$basePropertyType"/>.class,
                                                                <xsl:value-of select="$listName"/>));

                                                 </xsl:when>
                                                 <xsl:otherwise>
                                                      <xsl:if test="@nillable">
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              <xsl:value-of select="$listName"/>.add(null);
                                                              reader.next();
                                                          } else {
                                                        </xsl:if>
                                                            <xsl:choose>
                                                                <!-- if the base property type is a soap encoding array then extension
                                                                  mapper class should also passed-->
                                                                <xsl:when test="$basePropertyType='org.apache.axis2.databinding.types.soapencoding.Array'">
                                                                    <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader,
                                                                                    <xsl:value-of select="$mapperClass"/>.class));
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                                                                </xsl:otherwise>
                                                            </xsl:choose>

                                                        <xsl:if test="@nillable">}</xsl:if>
                                                        //loop until we find a start element that is not part of this array
                                                        boolean <xsl:value-of select="$loopBoolName"/> = false;
                                                        while(!<xsl:value-of select="$loopBoolName"/>){
                                                            // We should be at the end element, but make sure
                                                            while (!reader.isEndElement())
                                                                reader.next();
                                                            // Step out of this element
                                                            reader.next();
                                                            // Step to next element event.
                                                            while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                                reader.next();
                                                            if (reader.isEndElement()){
                                                                //two continuous end elements means we are exiting the xml structure
                                                                <xsl:value-of select="$loopBoolName"/> = true;
                                                            } else {
                                                                if (<xsl:value-of select="$propQName"/>.equals(reader.getName())){
                                                                    <xsl:if test="@nillable">
                                                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                          <xsl:value-of select="$listName"/>.add(null);
                                                                          reader.next();
                                                                      } else {
                                                                    </xsl:if>
                                                                    <xsl:choose>
                                                                        <!-- if the base property type is a soap encoding array then extension
                                                                          mapper class should also passed-->
                                                                        <xsl:when test="$basePropertyType='org.apache.axis2.databinding.types.soapencoding.Array'">
                                                                            <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader,
                                                                                            <xsl:value-of select="$mapperClass"/>.class));
                                                                        </xsl:when>
                                                                        <xsl:otherwise>
                                                                            <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$basePropertyType"/>.Factory.parse(reader));
                                                                        </xsl:otherwise>
                                                                    </xsl:choose>
                                                                    <xsl:if test="@nillable">}</xsl:if>
                                                                }else{
                                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                                }
                                                            }
                                                        }
                                                        // call the converter utility  to convert and set the array
                                                        <xsl:choose>
                                                            <xsl:when test="$basePropertyType='java.lang.String'">
                                                                object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                                    <xsl:value-of select="$listName"/>.toArray(new <xsl:value-of select="$basePropertyType"/>[<xsl:value-of select="$listName"/>.size()]));
                                                            </xsl:when>
                                                            <xsl:otherwise>
                                                        object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                            org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                                <xsl:value-of select="$basePropertyType"/>.class,
                                                                <xsl:value-of select="$listName"/>));
                                                            </xsl:otherwise>
                                                        </xsl:choose>
                                                 </xsl:otherwise>
                                             </xsl:choose>
                                        </xsl:when>
                                        <!-- End of Array handling of ADB classes -->

                                        <!--Let's handle xs:any here-->
                                        <xsl:when test="@any">
                                           boolean <xsl:value-of select="$loopBoolName"/>=false;

                                             while (!<xsl:value-of select="$loopBoolName"/>){
                                                 event = reader.getEventType();
                                                 if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event){

                                                      // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                                                      org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/>
                                                         = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                                              new org.apache.axis2.util.StreamWrapper(reader), reader.getName());

                                                       <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$builderName"/>.getOMElement());
                                                        reader.next();
                                                        if (reader.isEndElement()) {
                                                            // we have two countinuos end elements
                                                           <xsl:value-of select="$loopBoolName"/> = true;
                                                        }

                                                 }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else{
                                                     reader.next();
                                                 }

                                             }

                                            <xsl:choose>
                                                <xsl:when test="$basePropertyType='java.lang.String'">
                                                    object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                        <xsl:value-of select="$listName"/>.toArray(new <xsl:value-of select="$basePropertyType"/>[<xsl:value-of select="$listName"/>.size()]));
                                                </xsl:when>
                                                <xsl:otherwise>
                                             object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                 org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                     <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>

                                        <!-- End of Array handling of ADB classes -->
                                        <xsl:when test="@default">

                                             boolean <xsl:value-of select="$loopBoolName"/>=false;
                                             javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                                    "<xsl:value-of select="$namespace"/>",
                                                    "<xsl:value-of select="$propertyName"/>");

                                             while (!<xsl:value-of select="$loopBoolName"/>){
                                                 event = reader.getEventType();
                                                 if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                                                         &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){

                                                      <!-- if-block that handles nillable -->
                                                      <xsl:if test="@nillable">
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              <xsl:value-of select="$listName"/>.add(null);
                                                              reader.next();
                                                          }else{
                                                      </xsl:if>
                                                           <xsl:value-of select="$listName"/>.add(org.apache.axis2.databinding.utils.ConverterUtil.getAnyTypeObject(reader,
                                                                            <xsl:value-of select="$mapperClass"/>.class));
                                                       <xsl:if test="@nillable">}</xsl:if>
                                                 } else if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event &amp;&amp;
                                                            !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event &amp;&amp;
                                                           !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else if (javax.xml.stream.XMLStreamConstants.END_DOCUMENT == event){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else{
                                                     reader.next();
                                                 }

                                             }

                                            <xsl:choose>
                                                <xsl:when test="$basePropertyType='java.lang.String'">
                                                    object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                        <xsl:value-of select="$listName"/>.toArray(new <xsl:value-of select="$basePropertyType"/>[<xsl:value-of select="$listName"/>.size()]));
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$listName"/>.toArray());
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>

                                        <!-- handling binary case -->
                                        <xsl:when test="@binary">
                                               <xsl:if test="@nillable">
                                                  nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                  if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                      <xsl:value-of select="$listName"/>.add(null);
                                                      reader.next();
                                                  } else {
                                                </xsl:if>

                                                    <xsl:value-of select="$listName"/>.add(org.apache.axiom.util.stax.XMLStreamReaderUtils.getDataHandlerFromElement(reader));

                                                <xsl:if test="@nillable">}</xsl:if>
                                                //loop until we find a start element that is not part of this array
                                                boolean <xsl:value-of select="$loopBoolName"/> = false;
                                                while(!<xsl:value-of select="$loopBoolName"/>){
                                                    // Ensure we are at the EndElement
                                                    while (!reader.isEndElement()){
                                                        reader.next();
                                                    }
                                                    // Step out of this element
                                                    reader.next();
                                                    // Step to next element event.
                                                    while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                        reader.next();
                                                    if (reader.isEndElement()){
                                                        //two continuous end elements means we are exiting the xml structure
                                                        <xsl:value-of select="$loopBoolName"/> = true;
                                                    } else {
                                                        if (<xsl:value-of select="$propQName"/>.equals(reader.getName())){
                                                             <xsl:if test="@nillable">
                                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                                  <xsl:value-of select="$listName"/>.add(null);
                                                                  reader.next();
                                                              } else {
                                                            </xsl:if>

                                                                <xsl:value-of select="$listName"/>.add(org.apache.axiom.util.stax.XMLStreamReaderUtils.getDataHandlerFromElement(reader));

                                                            <xsl:if test="@nillable">}</xsl:if>
                                                        }else{
                                                            <xsl:value-of select="$loopBoolName"/> = true;
                                                        }
                                                    }
                                                }
                                                // call the converter utility  to convert and set the array
                                            <xsl:choose>
                                                <xsl:when test="$basePropertyType='java.lang.String'">
                                                    object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                        <xsl:value-of select="$listName"/>.toArray(new <xsl:value-of select="$basePropertyType"/>[<xsl:value-of select="$listName"/>.size()]));
                                                </xsl:when>
                                                <xsl:otherwise>
                                                object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                            <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>

                                        <xsl:otherwise>
                                            <xsl:choose>
                                             <xsl:when test="@default">
                                             boolean <xsl:value-of select="$loopBoolName"/>=false;
                                             javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = new javax.xml.namespace.QName(
                                                    "<xsl:value-of select="$namespace"/>",
                                                    "<xsl:value-of select="$propertyName"/>");

                                             while (!<xsl:value-of select="$loopBoolName"/>){
                                                 event = reader.getEventType();
                                                 if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event
                                                         &amp;&amp; <xsl:value-of select="$startQname"/>.equals(reader.getName())){

                                                      <!-- if-block that handles nillable -->
                                                      <xsl:if test="@nillable">
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              <xsl:value-of select="$listName"/>.add(null);
                                                          }else{
                                                      </xsl:if>

                                                      // We need to wrap the reader so that it produces a fake START_DOCUEMENT event
                                                      org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/>
                                                         = new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                                              new org.apache.axis2.util.StreamWrapper(reader), <xsl:value-of select="$startQname"/>);

                                                       <xsl:value-of select="$listName"/>.add(<xsl:value-of select="$builderName"/>.getOMElement().getFirstElement());
                                                       <xsl:if test="@nillable">}</xsl:if>
                                                 } else if (javax.xml.stream.XMLStreamConstants.START_ELEMENT == event &amp;&amp;
                                                            !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else if (javax.xml.stream.XMLStreamConstants.END_ELEMENT == event &amp;&amp;
                                                           !<xsl:value-of select="$startQname"/>.equals(reader.getName())){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else if (javax.xml.stream.XMLStreamConstants.END_DOCUMENT == event){
                                                     <xsl:value-of select="$loopBoolName"/> = true;
                                                 }else{
                                                     reader.next();
                                                 }

                                             }

                                                 <xsl:choose>
                                                     <xsl:when test="$basePropertyType='java.lang.String'">
                                                         object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                             <xsl:value-of select="$listName"/>.toArray(new <xsl:value-of select="$basePropertyType"/>[<xsl:value-of select="$listName"/>.size()]));
                                                     </xsl:when>
                                                     <xsl:otherwise>
                                             object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                 org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                     <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));
                                                     </xsl:otherwise>
                                                 </xsl:choose>

                                        </xsl:when>
                                        <!-- End of Array handling of default class - that is the OMElement -->
                                        <xsl:otherwise>
                                             <xsl:if test="@nillable">
                                              nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                              if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                  <xsl:choose>
                                                       <xsl:when test="$basePropertyType='int'">
                                                           <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Integer.MIN_VALUE));
                                                       </xsl:when>
                                                       <xsl:when test="$basePropertyType='long'">
                                                           <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Long.MIN_VALUE));
                                                       </xsl:when>
                                                       <xsl:when test="$basePropertyType='byte'">
                                                           <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Byte.MIN_VALUE));
                                                       </xsl:when>
                                                       <xsl:when test="$basePropertyType='double'">
                                                           <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Double.NaN));
                                                       </xsl:when>
                                                       <xsl:when test="$basePropertyType='float'">
                                                           <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Float.NaN));
                                                       </xsl:when>
                                                       <xsl:when test="$basePropertyType='short'">
                                                           <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Short.MIN_VALUE));
                                                       </xsl:when>
                                                       <xsl:otherwise>
                                                           <xsl:value-of select="$listName"/>.add(null);
                                                       </xsl:otherwise>
                                                  </xsl:choose>
                                                  reader.next();
                                              } else {
                                            </xsl:if>
                                            <xsl:value-of select="$listName"/>.add(reader.getElementText());
                                            <xsl:if test="@nillable">}</xsl:if>
                                            //loop until we find a start element that is not part of this array
                                            boolean <xsl:value-of select="$loopBoolName"/> = false;
                                            while(!<xsl:value-of select="$loopBoolName"/>){
                                                // Ensure we are at the EndElement
                                                while (!reader.isEndElement()){
                                                    reader.next();
                                                }
                                                // Step out of this element
                                                reader.next();
                                                // Step to next element event.
                                                while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                                    reader.next();
                                                if (reader.isEndElement()){
                                                    //two continuous end elements means we are exiting the xml structure
                                                    <xsl:value-of select="$loopBoolName"/> = true;
                                                } else {
                                                    if (<xsl:value-of select="$propQName"/>.equals(reader.getName())){
                                                         <xsl:if test="@nillable">
                                                          nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                                          if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                                              <xsl:choose>
                                                                   <xsl:when test="$basePropertyType='int'">
                                                                       <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Integer.MIN_VALUE));
                                                                   </xsl:when>
                                                                   <xsl:when test="$basePropertyType='long'">
                                                                       <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Long.MIN_VALUE));
                                                                   </xsl:when>
                                                                   <xsl:when test="$basePropertyType='byte'">
                                                                       <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Byte.MIN_VALUE));
                                                                   </xsl:when>
                                                                   <xsl:when test="$basePropertyType='double'">
                                                                       <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Double.NaN));
                                                                   </xsl:when>
                                                                   <xsl:when test="$basePropertyType='float'">
                                                                       <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Float.NaN));
                                                                   </xsl:when>
                                                                   <xsl:when test="$basePropertyType='short'">
                                                                       <xsl:value-of select="$listName"/>.add(String.valueOf(java.lang.Short.MIN_VALUE));
                                                                   </xsl:when>
                                                                   <xsl:otherwise>
                                                                       <xsl:value-of select="$listName"/>.add(null);
                                                                   </xsl:otherwise>
                                                              </xsl:choose>
                                                              reader.next();
                                                          } else {
                                                        </xsl:if>
                                                        <xsl:value-of select="$listName"/>.add(reader.getElementText());
                                                        <xsl:if test="@nillable">}</xsl:if>
                                                    }else{
                                                        <xsl:value-of select="$loopBoolName"/> = true;
                                                    }
                                                }
                                            }
                                            // call the converter utility  to convert and set the array
                                            <xsl:choose>
                                                <xsl:when test="$basePropertyType='java.lang.String'">
                                                    object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                        <xsl:value-of select="$listName"/>.toArray(new <xsl:value-of select="$basePropertyType"/>[<xsl:value-of select="$listName"/>.size()]));
                                                </xsl:when>
                                                <xsl:otherwise>
                                            object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$propertyType"/>)
                                                org.apache.axis2.databinding.utils.ConverterUtil.convertToArray(
                                                            <xsl:value-of select="$basePropertyType"/>.class,<xsl:value-of select="$listName"/>));
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:otherwise>
                                        </xsl:choose>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:when test="@ours">
                                    <xsl:if test="@nillable">
                                      nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                      if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                          object.set<xsl:value-of select="$javaName"/>(null);
                                          reader.next();
                                          <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                            reader.next();
                                          </xsl:if>
                                      }else{
                                    </xsl:if>
                                        <xsl:choose>
                                            <!-- if the base property type is a soap encoding array then extension
                                              mapper class should also passed-->
                                            <xsl:when test="$propertyType='org.apache.axis2.databinding.types.soapencoding.Array'">
                                                object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/>.Factory.parse(reader,
                                                                <xsl:value-of select="$mapperClass"/>.class));
                                            </xsl:when>
                                            <xsl:otherwise>
                                                object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$propertyType"/>.Factory.parse(reader));
                                            </xsl:otherwise>
                                        </xsl:choose>

                                    <xsl:if test="($isType or $anon) and not($particleClassType)">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                    <xsl:if test="@nillable">}</xsl:if>
                                </xsl:when>
                                <!-- start of any handling. Any can also be @default so we need to handle the any case before default! -->
                                <xsl:when test="@any">
                                    <!--No concerns of being nillable here. if it's ours and if the nillable attribute was present
                                        we would have outputted a null already-->
                                     <!--This can be any element and we may not know the name. so we pick the name of the element from the parser-->
                                     //use the QName from the parser as the name for the builder
                                     javax.xml.namespace.QName <xsl:value-of select="$startQname"/> = reader.getName();

                                     // We need to wrap the reader so that it produces a fake START_DOCUMENT event
                                     // this is needed by the builder classes
                                     org.apache.axis2.databinding.utils.NamedStaxOMBuilder <xsl:value-of select="$builderName"/> =
                                         new org.apache.axis2.databinding.utils.NamedStaxOMBuilder(
                                             new org.apache.axis2.util.StreamWrapper(reader),<xsl:value-of select="$startQname"/>);
                                     object.set<xsl:value-of select="$javaName"/>(<xsl:value-of select="$builderName"/>.getOMElement());
                                     <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                         reader.next();
                                     </xsl:if>
                                </xsl:when>
                                <!-- end of adb type handling code -->
                                <!-- start of OMelement handling -->
                                 <xsl:when test="@default">
                                     object.set<xsl:value-of select="$javaName"/>(org.apache.axis2.databinding.utils.ConverterUtil.getAnyTypeObject(reader,
                                                <xsl:value-of select="$mapperClass"/>.class));
                                     <xsl:if test="$isType or $anon">  <!-- This is a subelement property to be consumed -->
                                         reader.next();
                                     </xsl:if>
                                </xsl:when>
                                <!-- end of OMelement handling -->
                                <!-- start of the simple types handling for binary content-->
                                <xsl:when test="@binary">

                                    <xsl:if test="@nillable">
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                             object.set<xsl:value-of select="$javaName"/>(null);
                                             reader.next();
                                        } else {
                                    </xsl:if>
                                            object.set<xsl:value-of select="$javaName"/>(org.apache.axiom.util.stax.XMLStreamReaderUtils.getDataHandlerFromElement(reader));
                                    <xsl:if test="@nillable">
                                        }
                                    </xsl:if>

                                    <xsl:if test="($isType or $anon) and not($simple)">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:when>
                                <!-- start of the simple types handling -->
                                <xsl:otherwise>
                                    <xsl:if test="@nillable">
                                       nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                       if (!"true".equals(nillableValue) &amp;&amp; !"1".equals(nillableValue)){
                                    </xsl:if>

                                    <!-- when the nillable = false in the element -->
                                    <xsl:if test="not(@nillable)">
                                    nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                    if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                        throw new org.apache.axis2.databinding.ADBException("The element: "+"<xsl:value-of select="$propertyName"/>" +"  cannot be null");
                                    }
                                    </xsl:if>

                                    java.lang.String content = reader.getElementText();
                                    <xsl:if test="not(enumFacet)">
                                        <xsl:choose>
                                            <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                                            int index = content.indexOf(":");
                                            if(index > 0){
                                                prefix = content.substring(0,index);
                                             } else {
                                                prefix = "";
                                             }
                                             namespaceuri = reader.getNamespaceURI(prefix);
                                             object.set<xsl:value-of select="$javaName"/>(
                                                  org.apache.axis2.databinding.utils.ConverterUtil.convertToQName(content,namespaceuri));
                                            </xsl:when>
                                            <xsl:when test="$propertyType='org.apache.axiom.om.OMElement'">
                                                org.apache.axiom.om.OMFactory fac = org.apache.axiom.om.OMAbstractFactory.getOMFactory();
                                                org.apache.axiom.om.OMNamespace omNs = fac.createOMNamespace("<xsl:value-of select="$namespace"/>", "");
                                                org.apache.axiom.om.OMElement _value<xsl:value-of select="$javaName"/> = fac.createOMElement("<xsl:value-of select="$propertyName"/>", omNs);
                                                _value<xsl:value-of select="$javaName"/>.addChild(fac.createOMText(_value<xsl:value-of select="$javaName"/>, content));
                                                object.set<xsl:value-of select="$javaName"/>(_value<xsl:value-of select="$javaName"/>);
                                            </xsl:when>
                                            <xsl:otherwise>
                                              object.set<xsl:value-of select="$javaName"/>(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertTo<xsl:value-of select="$shortTypeName"/>(content));
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:if>
                                    <xsl:if test="(enumFacet)">
                                        if (content.indexOf(":") > 0) {
                                            // this seems to be a Qname so find the namespace and send
                                            prefix = content.substring(0, content.indexOf(":"));
                                            namespaceuri = reader.getNamespaceURI(prefix);
                                            object = <xsl:value-of select="$name"/>.Factory.fromString(content,namespaceuri);
                                        } else {
                                            // this seems to be not a qname send and empty namespace incase of it is
                                            // check is done in fromString method
                                            object = <xsl:value-of select="$name"/>.Factory.fromString(content,"");
                                        }
                                        <!-- set the attribute values here since onbject is not initalized yet -->
                                        <xsl:for-each select="../property[@attribute]">
                                            <xsl:variable name="propertyName" select="@name"/>
                                            <xsl:variable name="propertyType" select="@type"/>
                                            <xsl:variable name="shortTypeNameUncapped"  select="@shorttypename"/>
                                            <xsl:variable name="shortTypeName"
                                            select="concat(translate(substring($shortTypeNameUncapped, 1, 1 ),'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ' ), substring($shortTypeNameUncapped, 2, string-length($shortTypeNameUncapped)))" />
                                            <xsl:variable name="javaName" select="@javaname"/>
                                            <xsl:variable name="attribName">tempObjectAttrib<xsl:value-of select="$propertyName"/></xsl:variable>

                                            <xsl:if test="$propertyName != 'extraAttributes'">
                                                // handle attribute "<xsl:value-of select="$propertyName"/>"
                                                java.lang.Object <xsl:value-of select="$attribName"/> =
                                                                attributeMap.get("<xsl:value-of select="$javaName"/>");
                                               if (<xsl:value-of select="$attribName"/>!=null){
                                                    <xsl:choose>
                                                        <xsl:when test="$propertyType='javax.xml.namespace.QName'">
                                                           object.set<xsl:value-of select="$javaName"/>((javax.xml.namespace.QName)<xsl:value-of select="$attribName"/>);
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            object.set<xsl:value-of select="$javaName"/>((<xsl:value-of select="$shortTypeName"/>)<xsl:value-of select="$attribName"/>);
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                }
                                            </xsl:if>

                                            <!-- Handle anyAttributes here -->
                                            <xsl:if test="$propertyName = 'extraAttributes'">
                                                for(org.apache.axiom.om.OMAttribute att : extraAttributeList){
                                                    object.addExtraAttributes(att);
                                                }
                                            </xsl:if>

                                        </xsl:for-each>

                                    </xsl:if>
                                    <xsl:if test="@nillable">
                                       } else {
                                           <!-- set the variable value according to the variable type -->
                                           <xsl:choose>
                                               <xsl:when test="$propertyType='int'">
                                                   object.set<xsl:value-of select="$javaName"/>(java.lang.Integer.MIN_VALUE);
                                               </xsl:when>
                                               <xsl:when test="$propertyType='long'">
                                                   object.set<xsl:value-of select="$javaName"/>(java.lang.Long.MIN_VALUE);
                                               </xsl:when>
                                               <xsl:when test="$propertyType='byte'">
                                                   object.set<xsl:value-of select="$javaName"/>(java.lang.Byte.MIN_VALUE);
                                               </xsl:when>
                                               <xsl:when test="$propertyType='double'">
                                                   object.set<xsl:value-of select="$javaName"/>(java.lang.Double.NaN);
                                               </xsl:when>
                                               <xsl:when test="$propertyType='float'">
                                                   object.set<xsl:value-of select="$javaName"/>(java.lang.Float.NaN);
                                               </xsl:when>
                                               <xsl:when test="$propertyType='short'">
                                                   object.set<xsl:value-of select="$javaName"/>(java.lang.Short.MIN_VALUE);
                                               </xsl:when>
                                           </xsl:choose>
                                           reader.getElementText(); // throw away text nodes if any.
                                       }
                                    </xsl:if>
                                    <xsl:if test="($isType or $anon) and not($simple)">  <!-- This is a subelement property to be consumed -->
                                        reader.next();
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:if test="not(enumFacet)">
                              }  // End of if for expected property start element
                                <xsl:if test="$min=0 and $ordered">
                                    else {
                                        <xsl:choose>
                                           <xsl:when test="$propertyType='int'">
                                               object.set<xsl:value-of select="$javaName"/>(java.lang.Integer.MIN_VALUE);
                                           </xsl:when>
                                           <xsl:when test="$propertyType='long'">
                                               object.set<xsl:value-of select="$javaName"/>(java.lang.Long.MIN_VALUE);
                                           </xsl:when>
                                           <xsl:when test="$propertyType='byte'">
                                               object.set<xsl:value-of select="$javaName"/>(java.lang.Byte.MIN_VALUE);
                                           </xsl:when>
                                           <xsl:when test="$propertyType='double'">
                                               object.set<xsl:value-of select="$javaName"/>(java.lang.Double.NaN);
                                           </xsl:when>
                                           <xsl:when test="$propertyType='float'">
                                               object.set<xsl:value-of select="$javaName"/>(java.lang.Float.NaN);
                                           </xsl:when>
                                           <xsl:when test="$propertyType='short'">
                                               object.set<xsl:value-of select="$javaName"/>(java.lang.Short.MIN_VALUE);
                                           </xsl:when>
                                       </xsl:choose>
                                    }
                                </xsl:if>
                            </xsl:if>
                            <!-- in a particle class all inner elements may be min=0 their validation done
                                 inside the sequce class -->
                            <xsl:if test="$ordered and $min!=0 and not($particleClassType)">
                                else{
                                    // A start element we are not expecting indicates an invalid parameter was passed
                                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                                }
                            </xsl:if>
                            <xsl:if test="$particleClassType and ($choice or ($min=0)) and not($shortTypeName='OMElement')">
                                <!-- since we can not validate the parser before going to next class
                                 we have to sollow an excpetions : todo find a better solsution-->
                                 } catch (java.lang.Exception e) {}
                            </xsl:if>
                        </xsl:for-each>

                        <xsl:if test="$ordered">  <!-- pick up trailing cruft after final property before outer endElement and verify no trailing properties -->
                            while (!reader.isStartElement() &amp;&amp; !reader.isEndElement())
                                reader.next();
                            <xsl:if test="not($particleClass)">
                                if (reader.isStartElement())
                                // A start element we are not expecting indicates a trailing invalid property
                                throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                            </xsl:if>
                        </xsl:if>

                        <xsl:if test="property[not(@attribute)]">  <!-- this if is needed to skip all this when there are no propoerties-->
                        <xsl:if test="$unordered and not($particleClass)">
                          <xsl:if test="not(property/enumFacet) and not($choice or $hasParticleType)">
                             else{
                                        // A start element we are not expecting indicates an invalid parameter was passed
                                        throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             }
                          </xsl:if>
                             } else {
                                reader.next();
                             }  <!-- At neither a start nor an end element, skip it -->
                           }  // end of while loop
                        </xsl:if>
                        </xsl:if>



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        <!-- end of template for choice/all -->

        }
           <!-- end of main template -->
    </xsl:template>
</xsl:stylesheet>