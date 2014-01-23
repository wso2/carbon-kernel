<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/class">
        <xsl:variable name="interfaceName"><xsl:value-of select="@interfaceName"/></xsl:variable>
        <xsl:variable name="callbackname"><xsl:value-of select="@callbackname"/></xsl:variable>
        <xsl:variable name="isSync"><xsl:value-of select="@isSync"/></xsl:variable>
        <xsl:variable name="isAsync"><xsl:value-of select="@isAsync"/></xsl:variable>
        <xsl:variable name="soapVersion"><xsl:value-of select="@soap-version"/></xsl:variable>
        <xsl:variable name="method-prefix"><xsl:value-of select="@prefix"/></xsl:variable>
        <xsl:variable name="qname"><xsl:value-of select="@qname"/></xsl:variable>
        <xsl:variable name="servicename"><xsl:value-of select="@servicename"/></xsl:variable>
        <xsl:variable name="caps_name"><xsl:value-of select="@caps-name"/></xsl:variable>
        <xsl:variable name="isServer"><xsl:value-of select="@isServer"/></xsl:variable>
        <xsl:variable name="outputlocation"><xsl:value-of select="@outputlocation"/></xsl:variable>
        <xsl:variable name="targetsourcelocation"><xsl:value-of select="@targetsourcelocation"/></xsl:variable>
        <xsl:variable name="option"><xsl:value-of select="@option"/></xsl:variable>
<VisualStudioProject
	ProjectType="Visual C++"
	Version="9.00"
	Keyword="Win32Proj"
	TargetFrameworkVersion="196613"
	>
<xsl:if test="$isServer='0'">
    <xsl:attribute name="Name"><xsl:value-of select="$servicename"/></xsl:attribute>
    <xsl:attribute name="RootNamespace">client</xsl:attribute>
</xsl:if>
<xsl:if test="$isServer='1'">
    <xsl:attribute name="Name"><xsl:value-of select="$servicename"/></xsl:attribute>
    <xsl:attribute name="RootNamespace">service</xsl:attribute>
</xsl:if>
<Platforms>
    <Platform Name="Win32"/>
	</Platforms>
	<ToolFiles>
	</ToolFiles>
	<Configurations>
		<Configuration
			Name="Debug|Win32"
			OutputDirectory="$(SolutionDir)$(ConfigurationName)"
			IntermediateDirectory="$(ConfigurationName)"
			CharacterSet="1"
			>
            <xsl:if test="$isServer='0'">
                <xsl:attribute name="ConfigurationType">1</xsl:attribute>
            </xsl:if>
            <xsl:if test="$isServer='1'">
                <xsl:attribute name="ConfigurationType">2</xsl:attribute>
            </xsl:if>
			<Tool Name="VCPreBuildEventTool"/>
			<Tool Name="VCCustomBuildTool"/>
			<Tool Name="VCXMLDataGeneratorTool"/>
			<Tool Name="VCWebServiceProxyGeneratorTool"/>
			<Tool Name="VCMIDLTool"/>
			<Tool
                Name="VCCLCompilerTool"
				Optimization="0"
				PreprocessorDefinitions="WIN32;_DEBUG;_WINDOWS;_USRDLL;SERVICE_EXPORTS;AXIS2_DECLARE_EXPORT"
				MinimalRebuild="true"
				BasicRuntimeChecks="3"
				RuntimeLibrary="3"
				UsePrecompiledHeader="0"
				WarningLevel="3"
				DebugInformationFormat="4"
			>
         <xsl:choose>
            <xsl:when test="$option=1">
                 <xsl:attribute name="AdditionalIncludeDirectories">.;.\<xsl:value-of select="$targetsourcelocation"/>;$(AXIS2C_HOME)\include;$(AXIS2C_HOME)\include\platforms;</xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                 <xsl:attribute name="AdditionalIncludeDirectories">.;$(AXIS2C_HOME)\include;$(AXIS2C_HOME)\include\platforms;</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
            </Tool>
			<Tool
				Name="VCManagedResourceCompilerTool"
			/>
			<Tool
				Name="VCResourceCompilerTool"
			/>
			<Tool
				Name="VCPreLinkEventTool"
			/>
			<Tool
				Name="VCLinkerTool"
				AdditionalDependencies="axiom.lib axutil.lib axis2_engine.lib"
				LinkIncremental="2"
				AdditionalLibraryDirectories="$(AXIS2C_HOME)\lib"
				GenerateDebugInformation="true"
				SubSystem="2"
				TargetMachine="1"
			/>
			<Tool
				Name="VCALinkTool"
			/>
			<Tool
				Name="VCManifestTool"
			/>
			<Tool
				Name="VCXDCMakeTool"
			/>
			<Tool
				Name="VCBscMakeTool"
			/>
			<Tool
				Name="VCFxCopTool"
			/>
			<Tool
				Name="VCAppVerifierTool"
			/>
			<Tool
				Name="VCPostBuildEventTool"
			/>
		</Configuration>
		<Configuration
			Name="Release|Win32"
			OutputDirectory="$(SolutionDir)$(ConfigurationName)"
			IntermediateDirectory="$(ConfigurationName)"
			CharacterSet="1"
			WholeProgramOptimization="1"
			>
            <xsl:if test="$isServer='0'">
                <xsl:attribute name="ConfigurationType">1</xsl:attribute>
            </xsl:if>
            <xsl:if test="$isServer='1'">
                <xsl:attribute name="ConfigurationType">2</xsl:attribute>
            </xsl:if>
			<Tool
				Name="VCPreBuildEventTool"
			/>
			<Tool
				Name="VCCustomBuildTool"
			/>
			<Tool
				Name="VCXMLDataGeneratorTool"
			/>
			<Tool
				Name="VCWebServiceProxyGeneratorTool"
			/>
			<Tool
				Name="VCMIDLTool"
			/>
			<Tool
				Name="VCCLCompilerTool"
				Optimization="2"
				EnableIntrinsicFunctions="true"
				PreprocessorDefinitions="WIN32;NDEBUG;_WINDOWS;_USRDLL;SERVICE_EXPORTS"
				RuntimeLibrary="2"
				EnableFunctionLevelLinking="true"
				UsePrecompiledHeader="0"
				WarningLevel="3"
				DebugInformationFormat="3"
			>
            <xsl:choose>
               <xsl:when test="$option=1">
                    <xsl:attribute name="AdditionalIncludeDirectories">.;.\<xsl:value-of select="$targetsourcelocation"/>;$(AXIS2C_HOME)\include;$(AXIS2C_HOME)\include\platforms;</xsl:attribute>
               </xsl:when>
               <xsl:otherwise>
                    <xsl:attribute name="AdditionalIncludeDirectories">.;$(AXIS2C_HOME)\include;$(AXIS2C_HOME)\include\platforms;</xsl:attribute>
               </xsl:otherwise>
           </xsl:choose>
            </Tool>
			<Tool
				Name="VCManagedResourceCompilerTool"
			/>
			<Tool
				Name="VCResourceCompilerTool"
			/>
			<Tool
				Name="VCPreLinkEventTool"
			/>
			<Tool
				Name="VCLinkerTool"
				AdditionalDependencies="axiom.lib axutil.lib axis2_engine.lib"
				LinkIncremental="1"
				AdditionalLibraryDirectories="$(AXIS2C_HOME)\lib"
				GenerateDebugInformation="true"
				SubSystem="2"
				OptimizeReferences="2"
				EnableCOMDATFolding="2"
				TargetMachine="1"
			/>
			<Tool
				Name="VCALinkTool"
			/>
			<Tool
				Name="VCManifestTool"
			/>
			<Tool
				Name="VCXDCMakeTool"
			/>
			<Tool
				Name="VCBscMakeTool"
			/>
			<Tool
				Name="VCFxCopTool"
			/>
			<Tool
				Name="VCAppVerifierTool"
			/>
			<Tool
				Name="VCPostBuildEventTool"
			/>
		</Configuration>
	</Configurations>
	<References>
	</References>
	<Files>
		<Filter
			Name="Source Files"
			Filter="cpp;c;cc;cxx;def;odl;idl;hpj;bat;asm;asmx"
			>
        <File>
                <xsl:attribute name="RelativePath">.\axis2_extension_mapper.c</xsl:attribute>
        </File>
        <xsl:if test="$isServer='0'">
             <File>
                <xsl:attribute name="RelativePath">.\<xsl:value-of select="@name"/>.c</xsl:attribute>
             </File>
        </xsl:if>
        <xsl:if test="$isServer='1'">
             <File>
                <xsl:attribute name="RelativePath">.\axis2_svc_skel_<xsl:value-of select="@servicename"/>.c</xsl:attribute>
             </File>
             <File>
                <xsl:attribute name="RelativePath">.\axis2_skel_<xsl:value-of select="@servicename"/>.c</xsl:attribute>
             </File>
        </xsl:if>
        <xsl:for-each select="method">
            <xsl:choose>
            <xsl:when test="$option=1">
                <xsl:for-each select="input/param[@type!='' and @ours ]">
                <xsl:variable name="inputtype" select="substring-before(@type, '_t*')"/>
                     <File>
                         <xsl:attribute name="RelativePath">.\<xsl:value-of select="$targetsourcelocation"/>\<xsl:value-of select='$inputtype'/>.c</xsl:attribute>
                    </File>
               </xsl:for-each>
                <xsl:for-each select="output/param[@type!='' and @ours]">
                 <xsl:variable name="outputtype1" select="substring-before(@type, '_t*')"/>
                        <File>
                         <xsl:attribute name="RelativePath">.\<xsl:value-of select="$targetsourcelocation"/>\<xsl:value-of select="$outputtype1"/>.c</xsl:attribute>
                    </File>
               </xsl:for-each>
                 <xsl:for-each select="fault/param[@type!='' and contains(@type, 'adb_')]">
                    <xsl:variable name="faulttype" select="substring-before(@type, '_t*')"/>
                        <File>
                         <xsl:attribute name="RelativePath">.\<xsl:value-of select="$targetsourcelocation"/>\<xsl:value-of select="$faulttype"/>.c</xsl:attribute>
                        </File>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="input/param[@type!='' and @ours ]">
                <xsl:variable name="inputtype" select="substring-before(@type, '_t*')"/>
                     <File>
                         <xsl:attribute name="RelativePath">.\<xsl:value-of select='$inputtype'/>.c</xsl:attribute>
                    </File>
               </xsl:for-each>
                <xsl:for-each select="output/param[@type!='' and @ours]">
                 <xsl:variable name="outputtype1" select="substring-before(@type, '_t*')"/>
                        <File>
                         <xsl:attribute name="RelativePath">.\<xsl:value-of select="$outputtype1"/>.c</xsl:attribute>
                    </File>
               </xsl:for-each>
                 <xsl:for-each select="fault/param[@type!='' and contains(@type, 'adb_')]">
                    <xsl:variable name="faulttype" select="substring-before(@type, '_t*')"/>
                        <File>
                         <xsl:attribute name="RelativePath">.\<xsl:value-of select="$faulttype"/>.c</xsl:attribute>
                        </File>
                </xsl:for-each>
            </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
		</Filter>
		<Filter
			Name="Header Files"
			Filter="h;hpp;hxx;hm;inl;inc;xsd"
			>
            <File>
                <xsl:attribute name="RelativePath">.\axis2_extension_mapper.h</xsl:attribute>
            </File>
            <xsl:if test="$isServer='0'">
                        <File>
                           <xsl:attribute name="RelativePath">.\<xsl:value-of select="@name"/>.h</xsl:attribute>
                        </File>
                   </xsl:if>
                   <xsl:if test="$isServer='1'">
                        <File>
                           <xsl:attribute name="RelativePath">.\axis2_skel_<xsl:value-of select="@servicename"/>.h</xsl:attribute>
                        </File>
                   </xsl:if>
                   <xsl:for-each select="method">
                       <xsl:choose>
                       <xsl:when test="$option=1">
                           <xsl:for-each select="input/param[@type!='' and @ours ]">
                           <xsl:variable name="inputtype" select="substring-before(@type, '_t*')"/>
                                <File>
                                    <xsl:attribute name="RelativePath">.\<xsl:value-of select="$targetsourcelocation"/>\<xsl:value-of select='$inputtype'/>.h</xsl:attribute>
                               </File>
                          </xsl:for-each>
                           <xsl:for-each select="output/param[@type!='' and @ours]">
                            <xsl:variable name="outputtype1" select="substring-before(@type, '_t*')"/>
                                   <File>
                                    <xsl:attribute name="RelativePath">.\<xsl:value-of select="$targetsourcelocation"/>\<xsl:value-of select="$outputtype1"/>.h</xsl:attribute>
                               </File>
                          </xsl:for-each>
                            <xsl:for-each select="fault/param[@type!='' and contains(@type, 'adb_')]">
                               <xsl:variable name="faulttype" select="substring-before(@type, '_t*')"/>
                                   <File>
                                    <xsl:attribute name="RelativePath">.\<xsl:value-of select="$targetsourcelocation"/>\<xsl:value-of select="$faulttype"/>.h</xsl:attribute>
                                   </File>
                           </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                           <xsl:for-each select="input/param[@type!='' and @ours ]">
                           <xsl:variable name="inputtype" select="substring-before(@type, '_t*')"/>
                                <File>
                                    <xsl:attribute name="RelativePath">.\<xsl:value-of select='$inputtype'/>.h</xsl:attribute>
                               </File>
                          </xsl:for-each>
                           <xsl:for-each select="output/param[@type!='' and @ours]">
                            <xsl:variable name="outputtype1" select="substring-before(@type, '_t*')"/>
                                   <File>
                                    <xsl:attribute name="RelativePath">.\<xsl:value-of select="$outputtype1"/>.h</xsl:attribute>
                               </File>
                          </xsl:for-each>
                            <xsl:for-each select="fault/param[@type!='' and contains(@type, 'adb_')]">
                               <xsl:variable name="faulttype" select="substring-before(@type, '_t*')"/>
                                   <File>
                                    <xsl:attribute name="RelativePath">.\<xsl:value-of select="$faulttype"/>.h</xsl:attribute>
                                   </File>
                           </xsl:for-each>
                       </xsl:otherwise>
                       </xsl:choose>
                   </xsl:for-each>
		</Filter>
		<Filter
			Name="Resource Files"
			Filter="rc;ico;cur;bmp;dlg;rc2;rct;bin;rgs;gif;jpg;jpeg;jpe;resx;tiff;tif;png;wav">
		</Filter>
	</Files>
	<Globals>
	</Globals>
</VisualStudioProject>
 </xsl:template>

</xsl:stylesheet>