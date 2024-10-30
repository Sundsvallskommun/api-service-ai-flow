package se.sundsvall.ai.flow.integration.intric;

import static se.sundsvall.ai.flow.integration.intric.IntricIntegration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import se.sundsvall.ai.flow.integration.intric.model.Output;
import se.sundsvall.ai.flow.integration.intric.model.RunService;

@FeignClient(
	name = CLIENT_ID,
	configuration = IntricIntegrationConfiguration.class,
	url = "${integration.intric.base-url}")
interface IntricClient {

	/*
	 * Grundinformation om tjänsteskrivelsen
	 * 
	 * Först ska användaren lägga till viss grundinformation om tjänsteskrivelsen:
	 * 
	 * - Ärendenummer, t.ex. KS-2024-123456
	 * 
	 * - Uppdraget till tjänsten
	 * - En fritext där användaren får beskriva sitt uppdrag till tjänsten, exempelvis "Jag
	 * vill skriva en tjänsteskrivelse där vi föreslår en ny strategi för digital utveckling
	 * för Sundsvalls kommunkoncern" (fritextfält i webbapplikationen som skickas till
	 * mikrotjänsten som i sin tur bifogar användarens uppdrag i anropet till Intric API:et)
	 * 
	 * - Bakgrundsmaterial
	 * - Användaren kan ladda upp dokument som bakgrundsmaterial (tjänsten behöver "OCR:a"
	 * informationen till ren text (eller ??))
	 * 
	 * - Relaterade styrdokument
	 * - Användaren får ladda upp relaterade styrdokument till ärendet
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 1. Ärendet 9dda859f-f7cf-4961-9616-cdcb1c8b3d85
	 * 
	 * Här ska tjänsten sammanfatta ärendet, assistenten har en prompt som ser till att AI
	 * sammanfattar ärendet enligt regelverket i Sundsvalls kommun, men för att kunna göra det
	 * behöver webbtjänsten skicka med viss grunddata i sin input.
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * 
	 * Output:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 2. Bakgrund 127dd187-b010-42db-a0b4-f413de22963f
	 * 
	 * Här ska tjänsten skriva en längre och mer utförlig text rörande bakgrunden till ärendet,
	 * allt baserat på allt bifogat material.
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * 
	 * Output:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 3. Förvaltningens överväganden 714e598a-7a73-4870-81e5-1b8c9e3897a3
	 * 
	 * Här ska tjänsten sammanfatta förvaltningens överväganden kring ärendet.
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * - Texten som kom som output från rubriken "Bakgrund"
	 * Output:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 4. Styrdokument och juridik 7b0aaa43-74e9-4b46-9546-eb29f6ee8419
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * - Styrdokument (De dokument som användaren laddat upp som relaterade styrdokument ska skickas med)
	 * 
	 * Ouput:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 5. Ekonomisk hållbarhet 4acb5405-3526-4c31-b7a7-c50d444c57f2
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * 
	 * Output:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 6. Ekologisk hållbarhet f646c831-8424-494f-a833-68d64f452c22
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * 
	 * Output:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 7. Social hållbarhet a9c350c9-0e63-4e57-bc6a-7cf6a2a5f8c7
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * 
	 * Output:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 * 
	 * 8. Landsbygdssäkring 12b64b34-dec5-44d9-a758-beb55af81c56
	 * 
	 * Input:
	 * - Uppdraget till tjänsten (Det användaren har angivit som uppdrag till tjänsten under grundinformation)
	 * - Förvaltningens input (Det användaren har angivit som förvaltningens input under grundinformation)
	 * - Bakgrundsmaterial (Allt bakgrundsmaterial som användaren har laddat upp ska skickas med)
	 * 
	 * Output:
	 * - Sparas och visas i tjänsten
	 * 
	 * ------------------------------------------------------------------------------------------
	 */

	@PostMapping(value = "/services/{serviceId}/run/")
	Output runService(@PathVariable("serviceId") String serviceId, @RequestBody RunService input);
}
