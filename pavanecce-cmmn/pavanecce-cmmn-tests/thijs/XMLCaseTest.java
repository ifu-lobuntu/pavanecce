package test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.cafienne.cmmn.instance.Case;
import org.cafienne.cmmn.instance.CaseRepository;
import org.cafienne.cmmn.instance.PlanItem;
import org.cafienne.cmmn.instance.Transition;
import org.cafienne.engine.Engine;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import util.XMLHelper;

public class XMLCaseTest
{
	// Storage locations
	private static final String definitionFolder = "testdefinition/";
	private static final String testFolder = "testscript/";
	private static final String resultFolder = "testresult/";

	private final Engine engine;
	private Case caseInstance;
	private final ApplicationContext context;
	private int actionNumber;

	private Document testScriptDocument;
	private Document testResultDocument;

	public XMLCaseTest(String caseName, ApplicationContext context)
	{
		this.engine = new Engine("test");
		this.context = context;
		try
		{
			this.caseInstance = engine.createCase(engine, definitionFolder + caseName + ".case", context);
		}
		catch (IOException | ParserConfigurationException | SAXException e)
		{
			throw new RuntimeException("Cannot instantiate test case '" + caseName + "'", e);
		}
	}

	public void runTest(String testName, boolean... onlyRecordResults)
	{
		System.out.println("\n\nRunning test case " + testName);
		persistAndReloadCase();

		try
		{
			testScriptDocument = XMLHelper.getXMLDocument(testFolder + testName + ".casetest");
			testResultDocument = XMLHelper.loadXML("<test />");
		}
		catch (Exception e)
		{
			throw new RuntimeException("Could not load test script " + testName, e);
		}

		try
		{
			actionNumber = 0;
			Collection<Element> actionElements = XMLHelper.getElementsByTagName(testScriptDocument.getDocumentElement(), "action");
			for (Element element : actionElements)
			{
				actionNumber++;
				executeAction(element, onlyRecordResults);
			}
		}
		finally
		{
			System.out.println("\n\nEnding test case " + testName);
			try
			{
				File resultDirectory = new File(resultFolder);
				resultDirectory.mkdirs();
				XMLHelper.persist(testResultDocument, new File(resultFolder + testName + ".result.xml"));
			}
			catch (Exception e)
			{
				throw new RuntimeException("Could not persist the test results", e);
			}
		}
	}

	private void executeAction(Element actionElement, boolean... onlyRecordResults)
	{
		// Remove the expected result from the testResult document and replace
		// it with an empty clone of the action 
		Element clonedAction = (Element) testResultDocument.importNode(actionElement, true);
		Element clonedExpectedResult = XMLHelper.getElement(clonedAction, "result");
		if (clonedExpectedResult == null)
		{
			clonedExpectedResult = testResultDocument.createElement("result");
		}
		else
		{
			Node child = clonedExpectedResult.getFirstChild();
			while (child != null)
			{
				clonedExpectedResult.removeChild(child);
				child = clonedExpectedResult.getFirstChild();
			}
		}
		
		System.out.println("\n\nExecuting action "+actionNumber+":\n"+XMLHelper.printXMLNode(clonedAction));
		doAction(actionElement);

		Element expectedResult = XMLHelper.getElement(actionElement, "result");
		Element actualCase = caseInstance.dumpMemoryStateToXML().getDocumentElement();

		Node clonedActualResult = testResultDocument.importNode(actualCase, true);
		clonedExpectedResult.appendChild(clonedActualResult);
		testResultDocument.getDocumentElement().appendChild(clonedAction);

		if ((onlyRecordResults.length == 0 || !onlyRecordResults[0]) && expectedResult != null)
		{
			XMLHelper.assertContains(XMLHelper.getElement(expectedResult, "Case"), actualCase, "Failure during action "+actionNumber, "id", "name", "source");
		}
	}
	
	private void doAction(Element actionElement)
	{
		Element planItemAction = XMLHelper.getElement(actionElement, "planItem");
		if (planItemAction != null)
		{
			executePlanItemAction(planItemAction);
		}
		Element caseAction = XMLHelper.getElement(actionElement, "case");
		if (caseAction != null)
		{
			executeCaseAction(caseAction);
		}
		Element discretionaryAction = XMLHelper.getElement(actionElement, "discretionaryItem");
		if (discretionaryAction != null)
		{
			executeDiscretionaryItemAction(discretionaryAction);
		}
	}

	private void executePlanItemAction(Element action)
	{
		String name = XMLHelper.getContent(action, "name", "");
		String transition = XMLHelper.getContent(action, "transition", "");
		Collection<PlanItem> planItems = caseInstance.getPlanItems();
		for (PlanItem planItem : planItems)
		{
			if (planItem.getName().equals(name))
			{
				planItem.makeTransition(Transition.valueOf(transition));
			}
		}
	}

	private void executeCaseAction(Element action)
	{
		String transition = XMLHelper.getContent(action, "transition", "");
		caseInstance.transition(Transition.valueOf(transition));
	}

	private void executeDiscretionaryItemAction(Element action)
	{
		// String name = XMLHelper.getContent(action, "name", "");
		throw new RuntimeException("Discretionary Items cannot yet be planned");
	}

	private void persistAndReloadCase()
	{
		if (context == null)
		{
			return;
		}

		CaseRepository caseRepository = context.getBean(CaseRepository.class);
		caseRepository.save(caseInstance);

		Document memoryStateBeforePersistence = caseInstance.dumpMemoryStateToXML();

		Case newInstance = caseRepository.findOne(caseInstance.getId());
		newInstance.setupMemoryStructure(caseInstance.getDefinition());

		Document memoryStateAfterPersistence = caseInstance.dumpMemoryStateToXML();

		XMLHelper.assertContains(memoryStateBeforePersistence.getDocumentElement(), memoryStateAfterPersistence.getDocumentElement(), "id", "name", "source");
		XMLHelper.assertContains(memoryStateAfterPersistence.getDocumentElement(), memoryStateBeforePersistence.getDocumentElement(), "id", "name", "source");
	}

}
