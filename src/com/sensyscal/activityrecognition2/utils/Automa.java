package com.sensyscal.activityrecognition2.utils;

public class Automa {
	
	public static final int STATE_STANDING = ClassesCodes.STANDING;
	public static final int STATE_SITTING = ClassesCodes.SITTING;
	public static final int STATE_WALKING = ClassesCodes.STANDING_WALKING;
	public static final int STATE_LYING_DOWN = ClassesCodes.LYING;
	public static final int STATE_STAND_BY = -1;
	public static int current_state = STATE_STAND_BY;
	
	public static int nextState(int cls,int transition){
		int state = cls;
		switch (current_state) {
			
			//CURRENT STATE = STANDING
			case(STATE_STANDING):
				
				if(cls == STATE_STANDING && transition == TransitionsCodes.NO_TRANSITION)
					state = STATE_STANDING;
				
				else if(cls == STATE_STANDING && transition == TransitionsCodes.TRANSITION_STANDING_SITTING)
					state = STATE_SITTING;
				
				else state = cls;
				break;
				
			//CURRENT STATE = SITTING 
			case(STATE_SITTING):
				
				if(cls == STATE_STANDING && transition == TransitionsCodes.NO_TRANSITION)
					state = STATE_SITTING;
				
				else if(cls == STATE_STANDING && transition == TransitionsCodes.TRANSITION_SITTING_STANDING)
					state = STATE_STANDING;

				else
					state = cls;
				
				break;

				
			//CURRENT STATE = WALKING
			case(STATE_WALKING):
					state = cls;
					break;
				
			//CURRENT STATE = LYING DOWN
			case(STATE_LYING_DOWN):
					state = cls;
					break;

			default:
				break;
		}
		
		current_state = state;
		return state;
	}
	
	public static String getCurrentState(){
		switch (current_state) {
		
		case STATE_STANDING:
			return "STANDING";
			
		case STATE_SITTING:
			return "SITTING";
			
		case STATE_WALKING:
			return "WALKING";
			
		case STATE_LYING_DOWN:
			return "LYING DOWN";
			
		default:
			return "STAND BY";
		}
	}
/*if(cls == STATE_SITTING)
	state = STATE_SITTING;

if(cls == STATE_WALKING)
	state = STATE_WALKING;

if(cls == STATE_LYING_DOWN)
	state = STATE_LYING_DOWN;*/

}
