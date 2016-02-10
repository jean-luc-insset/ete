<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : collationInterfaces.xsl
    Created on : 15 décembre 2014, 08:11
    Author     : jldeleage
    Description:
        Purpose of transformation follows.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="xml"/>

    <xsl:template match="* | /">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="Component">
        <xsl:comment>Composant abstrait</xsl:comment>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- Classe "concrète" : il faut chercher le contenu de toutes les
         classes abstraites h&eacute;rit&eacute;s -->
    <xsl:template match="Component[not(abstract) or abstract='false']">
        <xsl:comment> *** Classe non abstraite *** </xsl:comment>
        <xsl:copy>
            <xsl:copy-of select="@* | *"/>
        </xsl:copy>
    </xsl:template>




</xsl:stylesheet>
