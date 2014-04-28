package org.pavanecce;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.pavanecce.common.text.workspace.TextMetamodelTests;
import org.pavanecce.common.text.workspace.TextMetamodelVersionTests;
import org.pavanecce.common.text.workspace.VersionNumberTests;
import org.pavanecce.common.util.JavaReverseTests;
import org.pavanecce.common.util.NameConverterTests;
import org.pavanecce.uml.uml2code.AnyExpressionTests;
import org.pavanecce.uml.uml2code.CodeModelBuilderTests;
import org.pavanecce.uml.uml2code.CollectExpressionTests;
import org.pavanecce.uml.uml2code.CompiledJavaTests;
import org.pavanecce.uml.uml2code.CompiledJythonTests;
import org.pavanecce.uml.uml2code.ExecutingJavascriptTests;
import org.pavanecce.uml.uml2code.JavaScriptSourceTests;
import org.pavanecce.uml.uml2code.JavaSourceTests;
import org.pavanecce.uml.uml2code.OclLiteralExpressionTests;
import org.pavanecce.uml.uml2code.PythonSourceTests;
import org.pavanecce.uml.uml2code.RoundTripTests;
import org.pavanecce.uml.uml2code.SelectExpressionTests;
import org.pavanecce.uml.uml2java.collections.tests.ManyToManyCollectionTests;
import org.pavanecce.uml.uml2java.collections.tests.OneToManyCollectionTests;

@RunWith(Suite.class)
@SuiteClasses({ JavaReverseTests.class, NameConverterTests.class, 
	ManyToManyCollectionTests.class, OneToManyCollectionTests.class, 
	TextMetamodelTests.class,  TextMetamodelVersionTests.class, VersionNumberTests.class, 
	AnyExpressionTests.class, CodeModelBuilderTests.class, CollectExpressionTests.class, ExecutingJavascriptTests.class,
		JavaSourceTests.class, JavaScriptSourceTests.class,OclLiteralExpressionTests.class, RoundTripTests.class, SelectExpressionTests.class, PythonSourceTests.class,
		CompiledJythonTests.class,CompiledJavaTests.class })
public class PavanecceUmlTestSuite  {

}
