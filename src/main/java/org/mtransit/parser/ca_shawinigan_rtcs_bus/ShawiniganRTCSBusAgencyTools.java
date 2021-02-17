package org.mtransit.parser.ca_shawinigan_rtcs_bus;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mtransit.commons.CleanUtils;
import org.mtransit.commons.RegexUtils;
import org.mtransit.parser.DefaultAgencyTools;
import org.mtransit.parser.MTLog;
import org.mtransit.parser.Pair;
import org.mtransit.parser.SplitUtils;
import org.mtransit.parser.SplitUtils.RouteTripSpec;
import org.mtransit.parser.gtfs.data.GRoute;
import org.mtransit.parser.gtfs.data.GSpec;
import org.mtransit.parser.gtfs.data.GStop;
import org.mtransit.parser.gtfs.data.GTrip;
import org.mtransit.parser.gtfs.data.GTripStop;
import org.mtransit.parser.mt.data.MAgency;
import org.mtransit.parser.mt.data.MDirectionType;
import org.mtransit.parser.mt.data.MRoute;
import org.mtransit.parser.mt.data.MTrip;
import org.mtransit.parser.mt.data.MTripStop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.mtransit.parser.StringUtils.EMPTY;

// http://www.shawinigan.ca/Ville/donnees-ouvertes_195.html
// https://donnees-shawinigan.opendata.arcgis.com/
// https://jmap.shawinigan.ca/doc/photos/google_transit.zip
public class ShawiniganRTCSBusAgencyTools extends DefaultAgencyTools {

	public static void main(@NotNull String[] args) {
		new ShawiniganRTCSBusAgencyTools().start(args);
	}

	@Override
	public boolean defaultExcludeEnabled() {
		return true;
	}

	@NotNull
	@Override
	public String getAgencyName() {
		return "RTCS";
	}

	@NotNull
	@Override
	public Integer getAgencyRouteType() {
		return MAgency.ROUTE_TYPE_BUS;
	}

	private static final String SAINT_GEORGES_DE_CHAMPLAIN_SHORT = "St-Georges";
	private static final String SHAWINIGAN_SUD = "Shawinigan-Sud";

	private static final Pattern SAINT_GEORGES_DE_CHAMPLAIN = Pattern.compile("((^|\\W)(saint-georges-de-champlain)(\\W|$))", Pattern.CASE_INSENSITIVE);
	private static final String SAINT_GEORGES_DE_CHAMPLAIN_REPLACEMENT = "$2" + SAINT_GEORGES_DE_CHAMPLAIN_SHORT + "$4";

	@SuppressWarnings("DuplicateBranchesInSwitch")
	@NotNull
	@Override
	public String getRouteLongName(@NotNull GRoute gRoute) {
		int rsn = Integer.parseInt(gRoute.getRouteShortName());
		switch (rsn) {
		case 1:
			if ("Saint-Georges / Shawinigan-Sud, du lundi au vendredi".equalsIgnoreCase(gRoute.getRouteLongName())) {
				return "St-Georges / Shawinigan-Sud (lundi-vendredi)";
			}
			break;
		case 2:
			if ("Saint-Georges / Shawinigan-Sud, du lundi au vendredi".equalsIgnoreCase(gRoute.getRouteLongName())) {
				return "St-Georges / Shawinigan-Sud (lundi-vendredi)";
			}
			break;
		case 3:
			if ("Saint-Georges / Shawinigan-Sud, samedi, dimanche et ete".equalsIgnoreCase(gRoute.getRouteLongName())) {
				return "St-Georges / Shawinigan-Sud (samedi-dimanche-été)";
			}
			break;
		}
		throw new MTLog.Fatal("Unexpected route long name %s!", gRoute);
	}

	private static final String AGENCY_COLOR_BLUE = "003769"; // BLUE (from PNG logo)

	private static final String AGENCY_COLOR = AGENCY_COLOR_BLUE;

	@NotNull
	@Override
	public String getAgencyColor() {
		return AGENCY_COLOR;
	}

	private static final HashMap<Long, RouteTripSpec> ALL_ROUTE_TRIPS2;

	static {
		HashMap<Long, RouteTripSpec> map2 = new HashMap<>();
		//noinspection deprecation
		map2.put(1L, new RouteTripSpec(1L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SAINT_GEORGES_DE_CHAMPLAIN_SHORT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SHAWINIGAN_SUD) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(
								"259",
								"276",
								"99",
								"100"
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(
								"100",
								"101",
								"192",
								"259"
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(2L, new RouteTripSpec(2L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SAINT_GEORGES_DE_CHAMPLAIN_SHORT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SHAWINIGAN_SUD) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(
								"5", "6", "234", "240", //
								"241", "243", //
								"260", "52", //
								"53", "99", "100"
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(
								"100", "101", "141", //
								"142", "49", //
								"204", "206", //
								"207", "208", "254", "255", "5"
						)) //
				.compileBothTripSort());
		//noinspection deprecation
		map2.put(3L, new RouteTripSpec(3L, //
				MDirectionType.NORTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SAINT_GEORGES_DE_CHAMPLAIN_SHORT, //
				MDirectionType.SOUTH.intValue(), MTrip.HEADSIGN_TYPE_STRING, SHAWINIGAN_SUD) //
				.addTripSort(MDirectionType.NORTH.intValue(), //
						Arrays.asList(
								"259", "276", "99", "100"
						)) //
				.addTripSort(MDirectionType.SOUTH.intValue(), //
						Arrays.asList(
								"100", "101", "192", "259"
						)) //
				.compileBothTripSort());
		ALL_ROUTE_TRIPS2 = map2;
	}

	@Override
	public int compareEarly(long routeId, @NotNull List<MTripStop> list1, @NotNull List<MTripStop> list2, @NotNull MTripStop ts1, @NotNull MTripStop ts2, @NotNull GStop ts1GStop, @NotNull GStop ts2GStop) {
		if (ALL_ROUTE_TRIPS2.containsKey(routeId)) {
			return ALL_ROUTE_TRIPS2.get(routeId).compare(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop, this);
		}
		return super.compareEarly(routeId, list1, list2, ts1, ts2, ts1GStop, ts2GStop);
	}

	@NotNull
	@Override
	public ArrayList<MTrip> splitTrip(@NotNull MRoute mRoute, @Nullable GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return ALL_ROUTE_TRIPS2.get(mRoute.getId()).getAllTrips();
		}
		return super.splitTrip(mRoute, gTrip, gtfs);
	}

	@NotNull
	@Override
	public Pair<Long[], Integer[]> splitTripStop(@NotNull MRoute mRoute, @NotNull GTrip gTrip, @NotNull GTripStop gTripStop, @NotNull ArrayList<MTrip> splitTrips, @NotNull GSpec routeGTFS) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return SplitUtils.splitTripStop(mRoute, gTrip, gTripStop, routeGTFS, ALL_ROUTE_TRIPS2.get(mRoute.getId()), this);
		}
		return super.splitTripStop(mRoute, gTrip, gTripStop, splitTrips, routeGTFS);
	}

	@Override
	public void setTripHeadsign(@NotNull MRoute mRoute, @NotNull MTrip mTrip, @NotNull GTrip gTrip, @NotNull GSpec gtfs) {
		if (ALL_ROUTE_TRIPS2.containsKey(mRoute.getId())) {
			return; // split
		}
		throw new MTLog.Fatal("%s: Unexpected trip %s!", mRoute.getId(), gTrip);
	}

	@Override
	public boolean directionFinderEnabled() {
		return false; // DISABLED because direction_id NOT provided
	}

	@Override
	public boolean mergeHeadsign(@NotNull MTrip mTrip, @NotNull MTrip mTripToMerge) {
		throw new MTLog.Fatal("Unexpected trips to merge %s and %s.", mTrip, mTripToMerge);
	}

	@NotNull
	@Override
	public String cleanTripHeadsign(@NotNull String tripHeadsign) {
		tripHeadsign = SAINT_GEORGES_DE_CHAMPLAIN.matcher(tripHeadsign).replaceAll(SAINT_GEORGES_DE_CHAMPLAIN_REPLACEMENT);
		tripHeadsign = CleanUtils.SAINT.matcher(tripHeadsign).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		tripHeadsign = CleanUtils.cleanNumbers(tripHeadsign);
		tripHeadsign = CleanUtils.cleanBounds(Locale.FRENCH, tripHeadsign);
		tripHeadsign = CleanUtils.cleanStreetTypesFRCA(tripHeadsign);
		return CleanUtils.cleanLabelFR(tripHeadsign);
	}

	private static final Pattern START_WITH_FACE_A = Pattern.compile("^(face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern START_WITH_FACE_AU = Pattern.compile("^(face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern START_WITH_FACE = Pattern.compile("^(face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern SPACE_FACE_A = Pattern.compile("( face à )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern SPACE_WITH_FACE_AU = Pattern.compile("( face au )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
	private static final Pattern SPACE_WITH_FACE = Pattern.compile("( face )", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

	private static final Pattern[] START_WITH_FACES = new Pattern[]{START_WITH_FACE_A, START_WITH_FACE_AU, START_WITH_FACE};

	private static final Pattern[] SPACE_FACES = new Pattern[]{SPACE_FACE_A, SPACE_WITH_FACE_AU, SPACE_WITH_FACE};

	private static final Pattern DEVANT_ = CleanUtils.cleanWordsFR("devant");

	@NotNull
	@Override
	public String cleanStopName(@NotNull String gStopName) {
		gStopName = CleanUtils.CLEAN_ET.matcher(gStopName).replaceAll(CleanUtils.CLEAN_ET_REPLACEMENT);
		gStopName = CleanUtils.SAINT.matcher(gStopName).replaceAll(CleanUtils.SAINT_REPLACEMENT);
		gStopName = DEVANT_.matcher(gStopName).replaceAll(EMPTY);
		gStopName = RegexUtils.replaceAllNN(gStopName, START_WITH_FACES, CleanUtils.SPACE);
		gStopName = RegexUtils.replaceAllNN(gStopName, SPACE_FACES, CleanUtils.SPACE);
		gStopName = CleanUtils.cleanBounds(Locale.FRENCH, gStopName);
		gStopName = CleanUtils.cleanNumbers(gStopName);
		gStopName = CleanUtils.cleanStreetTypesFRCA(gStopName);
		return CleanUtils.cleanLabelFR(gStopName);
	}
}
