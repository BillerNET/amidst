package amidst.fragment;

import java.util.List;

import amidst.documentation.AmidstThread;
import amidst.documentation.CalledOnlyBy;
import amidst.documentation.NotThreadSafe;
import amidst.fragment.layer.LayerDeclaration;
import amidst.mojangapi.world.coordinates.CoordinatesInWorld;
import amidst.mojangapi.world.icon.WorldIcon;

@NotThreadSafe
public class ClosestWorldIconFinder {
	private final FragmentGraph graph;
	private final List<LayerDeclaration> layerDeclarations;
	private final CoordinatesInWorld positionInWorld;
	private WorldIcon closestIcon;
	private double closestDistanceSq;

	@CalledOnlyBy(AmidstThread.EDT)
	public ClosestWorldIconFinder(FragmentGraph graph,
			List<LayerDeclaration> layerDeclarations,
			CoordinatesInWorld positionInWorld, double maxDistanceInWorld) {
		this.graph = graph;
		this.layerDeclarations = layerDeclarations;
		this.positionInWorld = positionInWorld;
		this.closestIcon = null;
		this.closestDistanceSq = maxDistanceInWorld * maxDistanceInWorld;
		find();
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void find() {
		for (FragmentGraphItem fragmentGraphItem : graph) {
			Fragment fragment = fragmentGraphItem.getFragment();
			for (LayerDeclaration declaration : layerDeclarations) {
				if (declaration.isVisible()) {
					int layerId = declaration.getLayerId();
					for (WorldIcon icon : fragment.getWorldIcons(layerId)) {
						updateClosest(icon);
					}
				}
			}
		}
	}

	@CalledOnlyBy(AmidstThread.EDT)
	private void updateClosest(WorldIcon icon) {
		double distanceSq = icon.getCoordinates()
				.getDistanceSq(positionInWorld);
		if (closestDistanceSq > distanceSq) {
			closestDistanceSq = distanceSq;
			closestIcon = icon;
		}
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public boolean hasResult() {
		return closestIcon != null;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public WorldIcon getWorldIcon() {
		return closestIcon;
	}

	@CalledOnlyBy(AmidstThread.EDT)
	public double getDistance() {
		return Math.sqrt(closestDistanceSq);
	}
}
